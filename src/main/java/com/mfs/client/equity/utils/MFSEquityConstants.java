package com.mfs.client.equity.utils;

/**
 * <p>
 * This class contains commonly used/repeated constants in this project
 */
public interface MFSEquityConstants {
    String EQUITY_BANK_RW = "EQUITY_BANK_RW";
    String MFS_EQUITY_ADAPTER = "mfs_equity_adapter";

    String TRANSFER_MONEY_SERVICE = "TRANSFER_MONEY_API";
    String REMITTANCE_STATUS_SERVICE = "TRANSFER_MONEY_STATUS_API";

    String TRANSACTION_CONTROLLER_SWAGGER_TAG = "Transaction Controller";
    String TRANSACTION_CONTROLLER_SWAGGER_DESCRIPTION = "It handles endpoints for a transaction";

    String WS_URL = "webservice_url";

    String AGENT_ID = "agent_id";
    String PAYMENT_TYPE = "payment_type";

    String SALT = "vfs_salt";
    String KEY_PATH = "private_key_path";
    String KEY_ALIAS = "private_key_alias";
    String KEY_PWD = "private_key_pwd";
    String MOCK_TRANSFER = "MOCK_TRANSFER";
    String MOCK_STATUS = "MOCK_STATUS";
	String READ_TIMEOUT = "ws_read_timeout";
	String CONNECTION_TIMEOUT = "ws_connection_timeout";
}
