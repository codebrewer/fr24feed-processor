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

package org.codebrewer.fr24feedprocessor.basestation.domain;

/**
 * The {@link MessageType#STA Status Change} message types generated by BaseStation.
 *
 * Based on information from http://woodair.net/sbs/Article/Barebones42_Socket_Data.htm
 *
 * @see DomainUtils#isExpectedStatusMessageType(StatusMessageType)
 */
public enum StatusMessageType {
  /**
   * Position Lost
   */
  PL,

  /**
   * Signal Lost
   */
  SL,

  /**
   * Remove
   */
  RM,

  /**
   * Delete
   */
  AD,

  /**
   * OK/Reset
   */
  OK;
}
