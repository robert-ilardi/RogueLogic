/**
 * Created Sep 27, 2006
 */
package com.roguelogic.roguenet;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNAConstants {

  public static final String ALL_MESSAGE_SUBJECT = "*";

  public static final String RN_ICON_CLASSPATH = "resources/rn3.png";

  public static final String PROP_ROOT_PROPERTIES_FILE = "RootPropertiesFile";
  public static final String PROP_PLUG_INS_PROPERTIES_FILE = "PlugInsPropertiesFile";
  public static final String PROP_USER_PROPERTIES_FILE = "UserPropertiesFile";

  public static final String PROP_P2PHUB_ADDRESS = "P2PHubAddress";
  public static final String PROP_P2PHUB_PORT = "P2PHubPort";
  public static final String PROP_P2PHUB_KEY_FILE = "P2PHubKeyFile";
  public static final String PROP_P2PHUB_USERNAME = "P2PHubUsername";
  public static final String PROP_P2PHUB_PASSWORD = "P2PHubPassword";
  public static final String PROP_P2PHUB_HEART_BEAT_INTERVAL = "P2PHubHeartBeatInterval";

  public static final String PROP_DEVELOPER_MODE = "DeveloperMode";
  public static final String PROP_SHOW_EXIT = "ShowExit";

  public static final String PROP_INTEGRATED_P2P_HUB_SERVER_ENABLED = "IntegratedP2PHubServerEnabled";
  public static final String PROP_INTEGRATED_P2P_HUB_SERVER_AUTO_START = "IntegratedP2PHubServerAutoStart";
  public static final String PROP_INTEGRATED_P2P_HUB_SERVER_NAME = "IntegratedP2PHubServerName";
  public static final String PROP_INTEGRATED_P2P_HUB_SERVER_PORT = "IntegratedP2PHubServerPort";
  public static final String PROP_INTEGRATED_P2P_HUB_SERVER_KEY_FILE = "IntegratedP2PHubServerKeyFile";
  public static final String PROP_INTEGRATED_P2P_HUB_SERVER_PEER_FILE = "IntegratedP2PHubServerPeerFile";
  public static final String PROP_INTEGRATED_P2P_HUB_SERVER_STDOUT_FILE = "IntegratedP2PHubServerStdOutFile";
  public static final String PROP_INTEGRATED_P2P_HUB_SERVER_STDERR_FILE = "IntegratedP2PHubServerStdErrFile";
  public static final String PROP_INTEGRATED_P2P_HUB_SERVER_HEART_BEAT_TTL = "P2PHubHeartBeatTTL";

  public static final String PROP_STDOUT_FILE = "StdOutFile";
  public static final String PROP_STDERR_FILE = "StdErrFile";

  public static final String PROP_NET_EXPLORER_LOCAL_LOOPING = "NetworkExplorerLocalLooping";

  public static final String PROP_RNA_PLUG_IN_CONFIGURATION = "RNAPlugInConfiguration";
  public static final String PROP_EXTERNAL_PLUGIN_CLASSPATH_LIST = "ExternPlugInClasspathList";

  public static final String PROP_AGENT_NAME = "AgentName";

  public static final String PROP_ENTITLEMENTS_MANAGER_IMPL = "EntitlementsManagerImpl";

  public static final String RNA_INFO_DAEMON_SUBJECT = "RNAInfoDaemon.HostInfoChannel";
  public static final String RNA_INFO_DAEMON_INFO_LEVEL_PROP = "RNAInfoDaemon.InfoLevel";
  public static final String RNA_INFO_DAEMON_REPLY_SUBJECT_PROP = "RNAInfoDaemon.ReplySubject";
  public static final String RNA_INFO_DAEMON_TARGET_PLUG_IN = "RNAInfoDaemon.TargetPlugIn";
  public static final String RNA_INFO_DAEMON_ROUND_TRIP_TOKEN_PROP = "RNAInfoDaemon.RoundTripToken";

  public static final String RNA_INFO_DAEMON_INFO_LEVEL_BASIC = "BASIC";
  public static final String RNA_INFO_DAEMON_INFO_LEVEL_DETAILED = "DETAILED";
  public static final String RNA_INFO_DAEMON_INFO_LEVEL_PLUG_IN_DETAILS = "PLUG-IN-DETAILS";

  public static final String RNA_INFO_DAEMON_AGENT_NAME_PROP = "RNAInfoDaemon.Info.AgentName";
  public static final String RNA_INFO_DAEMON_UP_TIME_PROP = "RNAInfoDaemon.Info.UpTime";
  public static final String RNA_INFO_DAEMON_JVM_VERSION_PROP = "RNAInfoDaemon.Info.JVMVersion";
  public static final String RNA_INFO_DAEMON_OS_NAME_PROP = "RNAInfoDaemon.Info.OSName";
  public static final String RNA_INFO_DAEMON_TOTAL_HEAP_PROP = "RNAInfoDaemon.Info.TotalHeap";
  public static final String RNA_INFO_DAEMON_AVAILABLE_HEAP_PROP = "RNAInfoDaemon.Info.AvailableHeap";
  public static final String RNA_INFO_DAEMON_P2P_HUB_SESSION_TOKEN_PROP = "RNAInfoDaemon.Info.P2PHubSessionToken";
  public static final String RNA_INFO_DAEMON_P2P_HUB_USERNAME_PROP = "RNAInfoDaemon.Info.P2PHubUsername";
  public static final String RNA_INFO_DAEMON_RNA_VERSION_PROP = "RNAInfoDaemon.Info.RNAVersion";
  public static final String RNA_INFO_DAEMON_MESSAGES_SENT_CNT_PROP = "RNAInfoDaemon.Info.MesgSentCnt";
  public static final String RNA_INFO_DAEMON_MESSAGES_RECEIVED_CNT_PROP = "RNAInfoDaemon.Info.MesgReceivedCnt";
  public static final String RNA_INFO_DAEMON_PLUG_IN_LIST_PROP = "RNAInfoDaemon.Info.PlugInList";

  public static final String RNA_NAMING_DAEMON_SUBJECT = "RNANamingDaemon.NamingChannel";
  public static final String RNA_NAMING_DAEMON_QUERY_PROP = "RNANamingDaemon.Query";
  public static final String RNA_NAMING_DAEMON_REPLY_SUBJECT_PROP = "RNANamingDaemon.ReplySubject";
  public static final String RNA_NAMING_DAEMON_ROUND_TRIP_TOKEN_PROP = "RNANamingDaemon.RoundTripToken";
  public static final String RNA_NAMING_DAEMON_TARGET_ENTRY_NAME_PROP = "RNANamingDaemon.TargetEntryName";
  public static final String RNA_NAMING_DAEMON_TARGET_ENTRY_VALUE_PROP = "RNANamingDaemon.TargetEntryValue";

  public static final String RNA_NAMING_DAEMON_QUERY_ENTIRE_REGISTRY = "EntireRegistry";
  public static final String RNA_NAMING_DAEMON_QUERY_SPECIFIC_ENTRY = "SpecificEntry";

  public static final String NAMING_ENTRY_PROP_PREFIX = "Naming.Entry.";

  public static final String RNA_NAMING_ENTRY_NAME = "com.roguelogic.roguenet.Agent";
  public static final String P2P_HUB_USERNAME_NAMING_ENTRY_NAME = "com.roguelogic.roguenet.P2PHubUsername";

  public static final int RNA_INFO_DAEMON_SUMMARY_AGENT_NAME_POS = 0;
  public static final int RNA_INFO_DAEMON_SUMMARY_RNA_VERSION_POS = 1;
  public static final int RNA_INFO_DAEMON_SUMMARY_UP_TIME_POS = 2;
  public static final int RNA_INFO_DAEMON_SUMMARY_JVM_VERSION_POS = 3;
  public static final int RNA_INFO_DAEMON_SUMMARY_OS_NAME_POS = 4;
  public static final int RNA_INFO_DAEMON_SUMMARY_TOTAL_HEAP_POS = 5;
  public static final int RNA_INFO_DAEMON_SUMMARY_AVAILABLE_HEAP_POS = 6;
  public static final int RNA_INFO_DAEMON_SUMMARY_P2P_HUB_USERNAME_POS = 7;
  public static final int RNA_INFO_DAEMON_SUMMARY_P2P_HUB_SESSION_TOKEN_POS = 8;

  public static final int RNA_INFO_DAEMON_SUMMARY_FIELD_CNT = 9;

  public static final String[] RNA_SUMMARY_INFO_HEADER = { "Agent Name", "RNA Version", "Up Time", "JVM Version", "OS Type", "Max Heap (MB)", "Free Heap (MB)", "P2P Hub Username",
      "P2P Hub Session Token" };

  public static final String NETWORK_EXPLORER_REPLY_SUBJECT = "NetworkExplorer.HostInfoReplyChannel";

  public static final String PLUG_IN_INFO_PROP_PREFIX = "PlugIn.Info.";
  public static final String PLUG_IN_INFO_DEVELOPER_PROP_PREFIX = "PlugIn.DvlprInfo.";

  public static final String PLUG_IN_INFO_LOGICAL_NAME_PROP = "PlugIn.Info.LogicalName";
  public static final String PLUG_IN_INFO_VERSION_PROP = "PlugIn.Info.Version";
  public static final String PLUG_IN_INFO_DESCRIPTION_PROP = "PlugIn.Info.Description";
  public static final String PLUG_IN_INFO_DEVELOPER_PROP = "PlugIn.Info.Developer";
  public static final String PLUG_IN_INFO_URL_PROP = "PlugIn.Info.URL";
  public static final String PLUG_IN_INFO_COPYRIGHT_PROP = "PlugIn.Info.Copyright";

  public static final String NETWORK_EXPLORER_PLUG_IN_DETAILS_PROP_TABLE_TITLE = "Network Explorer ~ Plug-In Details";

  public static final String PLUG_IN_REMOTE_INSTALL_DAEMON_SUBJECT = "PlugInRemoteInstallDaemon.InstallationChannel";

  public static final String RN_INTEGRATOR_SUBJECT = "RNIntegrator.IntegrationChannel";
  public static final String RN_INTEGRATOR_ACTION_PROP = "_RNI.Action";
  public static final String RN_INTEGRATOR_ACTION_GET_APP_LIST = "GetAppList";
  public static final String RN_INTEGRATOR_ACTION_SEND_DATA = "SendData";

  public static final String SSO_RECEIVER_SUBJECT = "SSO.Receiver.Channel";
  public static final String SSO_ACTION_PROP = "SSO.Action";
  public static final String SSO_ACTION_LOGOUT = "LogOut";
  public static final String SSO_ACTION_LOGIN = "LogIn";
  public static final String SSO_SESSION_TOKEN_PROP = "SSO.Session.Token";
  public static final String SSO_USERNAME_PROP = "SSO.Username";
  public static final String SSO_PASSWORD_PROP = "SSO.Password";
  public static final String SSO_ACTION_STATUS_PROP = "SSO.Action.Status";
  public static final String SSO_MESSAGE_PROP = "SSO.Message";
  public static final String SSO_ACTION_STATUS_SUCCESS = "SUCCESS";
  public static final String SSO_ACTION_STATUS_FAILURE = "FAILURE";

  public static final String SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT = "SynchronousTransactionReceiver.Channel";
  public static final String SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP = "SynchronousTransactionReceiver.TransactionId";

  public static final String FILE_STREAM_TRANSMITTER_SUBJECT = "FileStream.Transmitter.Channel";
  public static final String FILE_STREAM_TRANSMITTER_ACTION_PROP = "Action";
  public static final String FILE_STREAM_TRANSMITTER_ACTION_INIT_TRANSFER = "Init.Transfer";
  public static final String FILE_STREAM_TRANSMITTER_ACTION_ABORT_TRANSFER = "Abort.Transfer";
  public static final String FILE_STREAM_TRANSMITTER_ACTION_INIT_CLIENT_REQUESTED_TRANSFER = "Init.ClientRequestedTransfer";
  public static final String FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP = "RequestFile";
  public static final String FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP = "ReplySubject";
  public static final String FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP = "ClientTransferId";
  public static final String FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP = "ServerTransferId";
  public static final String FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP = "PayloadType";
  public static final String FILE_STREAM_TRANSMITTER_MESSAGE_PROP = "Message";
  public static final String FILE_STREAM_TRANSMITTER_STATUS_PROP = "Status";
  public static final String FILE_STREAM_TRANSMITTER_STATUS_GRANTED = "Granted";
  public static final String FILE_STREAM_TRANSMITTER_STATUS_DENIED = "Denied";
  public static final String FILE_STREAM_TRANSMITTER_STATUS_ERROR = "Error";
  public static final String FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS = "AccessStatus";
  public static final String FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_FILE_INFO = "FileInfo";
  public static final String FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_FILE_DATA_SEGMENT = "FileDataSegment";
  public static final String FILE_STREAM_TRANSMITTER_FILE_SIZE_PROP = "FileSize";
  public static final String FILE_STREAM_TRANSMITTER_STATUS_ABORTED = "Aborted";
  public static final String FILE_STREAM_TRANSMITTER_STATUS_COMPLETED = "Completed";

  public static final String FILE_SHARE_SERVICE_SUBJECT = "FileShareService.InfoChannel";
  public static final String FILE_SHARE_SERVICE_ACTION_PROP = "Action";
  public static final String FILE_SHARE_SERVICE_ACTION_GET_USER_SHARED_DIRECTORIES = "GetUserSharedDirectories";
  public static final String FILE_SHARE_SERVICE_ACTION_GET_FILE_LIST = "GetFileList";
  public static final String FILE_SHARE_SERVICE_ACTION_RENAME_FILE = "RenameFile";
  public static final String FILE_SHARE_SERVICE_ACTION_DELETE_FILE = "DeleteFile";
  public static final String FILE_SHARE_SERVICE_USER_SHARED_DIRECTORY_LIST_PROP = "UserSharedDirectoryList";
  public static final String FILE_SHARE_SERVICE_REQUEST_DIR_PROP = "RequestDir";
  public static final String FILE_SHARE_SERVICE_REQUEST_FILE_PROP = "RequestFile";
  public static final String FILE_SHARE_SERVICE_FILE_LIST_PROP = "FileList";
  public static final String FILE_SHARE_SERVICE_NEW_FILENAME_PROP = "NewFilename";
  public static final String FILE_SHARE_SERVICE_STATUS_PROP = "Status";
  public static final String FILE_SHARE_SERVICE_STATUS_FAILURE = "FAILURE";
  public static final String FILE_SHARE_SERVICE_STATUS_SUCCESS = "SUCCESS";

  public static final String SHARED_DIRECTORY_ITEM_TYPE = "SharedDir";

  public static final String FILE_TRANSFER_UTILITY_SUBJECT = "FileTransferUtility.DataChannel";

  public static final String FILE_STREAM_RECEIVER_SUBJECT = "FileStream.Receiver.Channel";
  public static final String FILE_STREAM_RECEIVER_ACTION_PROP = "Action";
  public static final String FILE_STREAM_RECEIVER_ACTION_INIT_TRANSFER = "Init.Transfer";
  public static final String FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP = "UploadFile";
  public static final String FILE_STREAM_RECEIVER_REPLY_SUBJECT_PROP = "ReplySubject";
  public static final String FILE_STREAM_RECEIVER_CLIENT_TRANSFER_ID_PROP = "ClientTransferId";
  public static final String FILE_STREAM_RECEIVER_PAYLOAD_TYPE_PROP = "PayloadType";
  public static final String FILE_STREAM_RECEIVER_STATUS_PROP = "Status";
  public static final String FILE_STREAM_RECEIVER_STATUS_GRANTED = "Granted";
  public static final String FILE_STREAM_RECEIVER_STATUS_DENIED = "Denied";
  public static final String FILE_STREAM_RECEVIER_STATUS_ERROR = "Error";
  public static final String FILE_STREAM_RECEIVER_PAYLOAD_TYPE_ACCESS_STATUS = "AccessStatus";
  public static final String FILE_STREAM_RECEIVER_REVERSE_DOWNLOAD_FILE_PROP = "ReverseDownloadFile";

  public static final String ECHO_SERVICE_SUBJECT = "EchoService.DataChannel";

}
