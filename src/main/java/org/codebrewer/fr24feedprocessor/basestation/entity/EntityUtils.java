/*
 * Copyright 2018 Mark Scott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codebrewer.fr24feedprocessor.basestation.entity;

import java.time.Instant;
import org.codebrewer.fr24feedprocessor.basestation.domain.DomainUtils;
import org.codebrewer.fr24feedprocessor.basestation.domain.MessageType;
import org.codebrewer.fr24feedprocessor.basestation.domain.StatusMessageType;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Utility methods related to the {@code BaseStationMessage} entity type.
 */
public class EntityUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EntityUtils.class);
  private static final Geographic2DCoordinateReferenceSystem COORDINATE_REFERENCE_SYSTEM =
      CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);

  private static Instant parseTimestamp(String dateToken, String timeToken) {
    if (StringUtils.isEmpty(dateToken) || StringUtils.isEmpty(timeToken)) {
      throw new IllegalArgumentException(
          String.format("Date (%s) and time (%s) must be provided", dateToken, timeToken));
    }

    // Values such as '2017-12-23T16:01:15.4294967295Z' have been seen, which causes a
    // DateTimeParseException, so truncate after 3 decimals
    //
    final int dotPosition = timeToken.indexOf('.');

    if (timeToken.length() > dotPosition + 4) {
      timeToken = timeToken.substring(0, 12);
    }

    return Instant.parse(String.format("%sT%sZ", dateToken.replaceAll("/", "-"), timeToken));
  }

  private static Short tokenAsShort(String token) {
    try {
      return Short.parseShort(token);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Point<G2D> tokensAsPosition(String lon, String lat) {
    try {
      return new Point<>(
          new G2D(Double.parseDouble(lon), Double.parseDouble(lat)),
          COORDINATE_REFERENCE_SYSTEM);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Float tokenAsFloat(String token) {
    try {
      return Float.parseFloat(token);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Boolean tokenAsBoolean(String token) {
    try {
      return Integer.parseInt(token) != 0;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Creates a {@code BaseStationMessage} from its comma-separated value text representation as
   * received on the incoming message feed.
   *
   * @param csvMessageText the comma-separated value text representation of a message, not null
   *
   * @return a {@code BaseStationMessage} created by parsing the CSV message text
   */
  public static BaseStationMessage fromCsvMessageText(String csvMessageText) {
    final String[] tokens =
        StringUtils.commaDelimitedListToStringArray(StringUtils.trimWhitespace(csvMessageText));

    if (tokens.length == 0) {
      LOGGER.warn("Message token array has zero length");

      return null;
    }

    final MessageType messageType = MessageType.valueOf(tokens[0]);

    if (!DomainUtils.isExpectedMessageType(messageType)) {
      LOGGER.error("Unexpected message type: '{}'", messageType);

      return null;
    }

    final int requiredTokenCount = messageType.getMessageTokenCount();

    if (tokens.length < requiredTokenCount) {
      throw new IllegalArgumentException(
          String.format("Expected %d tokens but found %d", requiredTokenCount, tokens.length));
    }

    final String icaoAddress = tokens[4];
    final Instant creationTimestamp = parseTimestamp(tokens[6], tokens[7]);
    final Instant receptionTimestamp = parseTimestamp(tokens[8], tokens[9]);

    switch (messageType) {
      case AIR:
        final NewAircraftMessage.Builder newAircraftMessageBuilder =
            new NewAircraftMessage.Builder(icaoAddress, creationTimestamp, receptionTimestamp);

        return newAircraftMessageBuilder.build();
      case ID:
        final IdMessage.Builder idMessageBuilder =
            new IdMessage.Builder(icaoAddress, creationTimestamp, receptionTimestamp);

        return idMessageBuilder.callSign(tokens[10]).build();
      case MSG:
        final Short transmissionType = tokenAsShort(tokens[1]);

        if (transmissionType == null) {
          LOGGER.error("Unable to parse transmission type: '{}'", tokens[1]);

          return null;
        }

        final TransmissionMessage.Builder transmissionMessageBuilder =
            new TransmissionMessage.Builder(
                icaoAddress, creationTimestamp, receptionTimestamp, transmissionType);

        switch (transmissionType) {
          case 1: // Identification message
            transmissionMessageBuilder.callSign(tokens[10]);
            break;
          case 2: // Surface position message
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .groundSpeed(tokenAsFloat(tokens[12]))
                .track(tokenAsFloat(tokens[13]))
                .position(tokensAsPosition(tokens[15], tokens[14]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case 3: // Airborne position message
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .position(tokensAsPosition(tokens[15], tokens[14]))
                .alert(tokenAsBoolean(tokens[18]))
                .emergency(tokenAsBoolean(tokens[19]))
                .identActive(tokenAsBoolean(tokens[20]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case 4: // Airborne velocity message
            transmissionMessageBuilder
                .groundSpeed(tokenAsFloat(tokens[12]))
                .track(tokenAsFloat(tokens[13]))
                .verticalRate(tokenAsShort(tokens[16]));
            break;
          case 5: // Surveillance altitude message
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .alert(tokenAsBoolean(tokens[18]))
                .identActive(tokenAsBoolean(tokens[20]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case 6: // Surveillance identification message
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .squawk(tokenAsShort(tokens[17]))
                .alert(tokenAsBoolean(tokens[18]))
                .emergency(tokenAsBoolean(tokens[19]))
                .identActive(tokenAsBoolean(tokens[20]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case 7: // Air to air message
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case 8: // "All call" reply message
            transmissionMessageBuilder
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          default:
            LOGGER.error("Unexpected transmission message type received: '{}'", transmissionType);

            return null;
        }

        return transmissionMessageBuilder.build();
      case STA:
        final StatusMessageType statusMessageType = StatusMessageType.valueOf(tokens[10]);

        if (!DomainUtils.isExpectedStatusMessageType(statusMessageType)) {
          LOGGER.error("Unexpected status message type: '{}'", statusMessageType);

          return null;
        }

        final StatusMessage.Builder statusMessageBuilder =
            new StatusMessage.Builder(
                icaoAddress, creationTimestamp, receptionTimestamp, statusMessageType);

        return statusMessageBuilder.build();
      default:
        LOGGER.error("Unexpected message type received: '{}'", messageType);

        return null;
    }
  }

  private EntityUtils() {
    // Utility class
  }
}