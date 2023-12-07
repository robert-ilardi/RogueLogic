/*
 * Created on Oct 25, 2005
 */
package com.roguelogic.net.rltalk;

/**
 * @author rilardi
 */

public final class ProtocolConstants {
  //General Codes
  public static final byte[] PROTOCOL_VERSION = { 53 }; //"5".getBytes();

  public static final int PROTOCOL_COMMAND_CODE_LIMIT = 99;

  //Payload Data Types
  public static final byte PAYLOAD_DATA_TYPE_BINARY = 48; //0

  public static final byte PAYLOAD_DATA_TYPE_SHORT = 49; //1

  public static final byte PAYLOAD_DATA_TYPE_CHAR = 50; //2

  public static final byte PAYLOAD_DATA_TYPE_INTEGER = 51; //3

  public static final byte PAYLOAD_DATA_TYPE_FLOAT = 52; //4

  public static final byte PAYLOAD_DATA_TYPE_LONG = 53; //5

  public static final byte PAYLOAD_DATA_TYPE_DOUBLE = 54; //6

  public static final byte PAYLOAD_DATA_TYPE_STRING = 55; //7

  public static final byte PAYLOAD_DATA_TYPE_OBJECT = 56; //8

  //End of Message Indicator
  public static final byte PACKET_COMPLETES_MESSAGE = 67; //C (Complete)

  public static final byte PACKET_MESSAGE_SEGMENT = 83; //S (Segment)

  //Lengths & Sizes
  public static final int PACKET_HEADER_LENGTH = 16;

  public static final int PACKET_VERSION_SEGMENT_LENGTH = 1;

  public static final int PACKET_PAYLOAD_SIZE_SEGMENT_LENGTH = 5;

  public static final int PACKET_COMMAND_SEGMENT_LENGTH = 4;

  public static final int PACKET_PAYLOAD_TYPE_INDICATOR_LENGTH = 1;

  public static final int PACKET_MULTIPLEXER_INDEX_LENGTH = 3;

  public static final int PACKET_END_OF_MESSAGE_INDICATOR_LENGTH = 1;

  public static final int STATUS_FIELD_LENGTH = 1;

  public static final int PROTOCOL_MULTIPLEXER_INDEX_LIMIT = 999;

  public static final int PACKET_PAYLOAD_MAX_SIZE = 65520;

  //public static final int PACKET_PAYLOAD_MAX_SIZE = 11;
  public static final int TOTAL_PACKET_SIZE = PACKET_HEADER_LENGTH + PACKET_PAYLOAD_MAX_SIZE;

  //Positions
  public static final int HEADER_START_INDEX = 0;

  public static final int HEADER_END_INDEX = HEADER_START_INDEX + (PACKET_HEADER_LENGTH - 1);

  public static final int VERSION_START_INDEX = HEADER_START_INDEX;

  public static final int VERSION_END_INDEX = VERSION_START_INDEX + (PACKET_VERSION_SEGMENT_LENGTH - 1);

  public static final int COMMAND_START_INDEX = VERSION_END_INDEX + 1;

  public static final int COMMAND_END_INDEX = COMMAND_START_INDEX + (PACKET_COMMAND_SEGMENT_LENGTH - 1);

  public static final int PAYLOAD_TYPE_INDICATOR_START_INDEX = COMMAND_END_INDEX + 1;

  public static final int PAYLOAD_TYPE_INDICATOR_END_INDEX = PAYLOAD_TYPE_INDICATOR_START_INDEX + (PACKET_PAYLOAD_TYPE_INDICATOR_LENGTH - 1);

  public static final int MULTIPLEXER_INDEX_START_INDEX = PAYLOAD_TYPE_INDICATOR_END_INDEX + 1;

  public static final int MULTIPLEXER_INDEX_END_INDEX = MULTIPLEXER_INDEX_START_INDEX + (PACKET_MULTIPLEXER_INDEX_LENGTH - 1);

  public static final int PAYLOAD_LENGTH_START_INDEX = MULTIPLEXER_INDEX_END_INDEX + 1;

  public static final int PAYLOAD_LENGTH_END_INDEX = PAYLOAD_LENGTH_START_INDEX + (PACKET_PAYLOAD_SIZE_SEGMENT_LENGTH - 1);

  public static final int STATUS_FIELD_START_INDEX = PAYLOAD_LENGTH_END_INDEX + 1;

  public static final int STATUS_FIELD_END_INDEX = STATUS_FIELD_START_INDEX + (STATUS_FIELD_LENGTH - 1);

  public static final int END_OF_MESSAGE_INDICATOR_START_INDEX = STATUS_FIELD_END_INDEX + 1;

  public static final int END_OF_MESSAGE_END_INDEX = END_OF_MESSAGE_INDICATOR_START_INDEX + (PACKET_END_OF_MESSAGE_INDICATOR_LENGTH - 1);

  public static final int PAYLOAD_START_INDEX = PACKET_HEADER_LENGTH;

  public static final int PAYLOAD_END_INDEX = TOTAL_PACKET_SIZE - 1;

  public static final int STATUS_CODE_LIMIT = 9;

  public static void main(String[] args) {
    StringBuffer sb = new StringBuffer();

    sb.append("RL-Talk Protocol Constants:\n");
    sb.append("---------------------------\n");

    sb.append("Protocol Version: ");
    sb.append(new String(PROTOCOL_VERSION));
    sb.append("\n");

    sb.append("Payload Data Type Binary: ");
    sb.append(PAYLOAD_DATA_TYPE_BINARY);
    sb.append("\n");

    sb.append("Payload Data Type Short: ");
    sb.append(PAYLOAD_DATA_TYPE_SHORT);
    sb.append("\n");

    sb.append("Payload Data Type Char: ");
    sb.append(PAYLOAD_DATA_TYPE_CHAR);
    sb.append("\n");

    sb.append("Payload Data Type Integer: ");
    sb.append(PAYLOAD_DATA_TYPE_INTEGER);
    sb.append("\n");

    sb.append("Payload Data Type Float: ");
    sb.append(PAYLOAD_DATA_TYPE_FLOAT);
    sb.append("\n");

    sb.append("Payload Data Type Long: ");
    sb.append(PAYLOAD_DATA_TYPE_LONG);
    sb.append("\n");

    sb.append("Payload Data Type Double: ");
    sb.append(PAYLOAD_DATA_TYPE_DOUBLE);
    sb.append("\n");

    sb.append("Payload Data Type String: ");
    sb.append(PAYLOAD_DATA_TYPE_STRING);
    sb.append("\n");

    sb.append("Payload Data Type Object: ");
    sb.append(PAYLOAD_DATA_TYPE_OBJECT);
    sb.append("\n");

    sb.append("Packet Header Length: ");
    sb.append(PACKET_HEADER_LENGTH);
    sb.append("\n");

    sb.append("Packet Version Segment Length: ");
    sb.append(PACKET_VERSION_SEGMENT_LENGTH);
    sb.append("\n");

    sb.append("Packet Payload Size Segment Length: ");
    sb.append(PACKET_PAYLOAD_SIZE_SEGMENT_LENGTH);
    sb.append("\n");

    sb.append("Packet Command Segment Length: ");
    sb.append(PACKET_COMMAND_SEGMENT_LENGTH);
    sb.append("\n");

    sb.append("Packet Payload Type Indicator Length: ");
    sb.append(PACKET_PAYLOAD_TYPE_INDICATOR_LENGTH);
    sb.append("\n");

    sb.append("Packet Payload Array Indicator Length: ");
    sb.append(PACKET_MULTIPLEXER_INDEX_LENGTH);
    sb.append("\n");

    sb.append("Packet Payload Max Size: ");
    sb.append(PACKET_PAYLOAD_MAX_SIZE);
    sb.append("\n");

    sb.append("Total Packet Size: ");
    sb.append(TOTAL_PACKET_SIZE);
    sb.append("\n");

    sb.append("Packet Multiplexer Index Limit: ");
    sb.append(PROTOCOL_MULTIPLEXER_INDEX_LIMIT);
    sb.append("\n");

    sb.append("Status Field Length: ");
    sb.append(STATUS_FIELD_LENGTH);
    sb.append("\n");

    sb.append("Status Code Limit: ");
    sb.append(STATUS_CODE_LIMIT);
    sb.append("\n");

    sb.append("Header Start Index: ");
    sb.append(HEADER_START_INDEX);
    sb.append("\n");

    sb.append("Header End Index: ");
    sb.append(HEADER_END_INDEX);
    sb.append("\n");

    sb.append("Version Start Index: ");
    sb.append(VERSION_START_INDEX);
    sb.append("\n");

    sb.append("Version End Index: ");
    sb.append(VERSION_END_INDEX);
    sb.append("\n");

    sb.append("Command Start Index: ");
    sb.append(COMMAND_START_INDEX);
    sb.append("\n");

    sb.append("Command End Index: ");
    sb.append(COMMAND_END_INDEX);
    sb.append("\n");

    sb.append("Payload Type Indicator Start Index: ");
    sb.append(PAYLOAD_TYPE_INDICATOR_START_INDEX);
    sb.append("\n");

    sb.append("Payload Type Indicator End Index: ");
    sb.append(PAYLOAD_TYPE_INDICATOR_END_INDEX);
    sb.append("\n");

    sb.append("Multiplexer Index Start Index: ");
    sb.append(MULTIPLEXER_INDEX_START_INDEX);
    sb.append("\n");

    sb.append("Multiplexer Index End Index: ");
    sb.append(MULTIPLEXER_INDEX_END_INDEX);
    sb.append("\n");

    sb.append("Status Field Start Index: ");
    sb.append(STATUS_FIELD_START_INDEX);
    sb.append("\n");

    sb.append("Status Field End Index: ");
    sb.append(STATUS_FIELD_END_INDEX);
    sb.append("\n");

    sb.append("Payload Start Index: ");
    sb.append(PAYLOAD_START_INDEX);
    sb.append("\n");

    sb.append("Payload End Index: ");
    sb.append(PAYLOAD_END_INDEX);
    sb.append("\n");

    sb.append("Procotol Command Code Limit: ");
    sb.append(PROTOCOL_COMMAND_CODE_LIMIT);

    System.out.println(sb);
  }

}
