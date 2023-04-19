ALTER TYPE polyflow.DEVICE_STATE ADD ATTRIBUTE wallet_type VARCHAR CASCADE;

UPDATE polyflow.wallet_connected_event SET device.wallet_type = 'injected' WHERE (device).wallet_type IS NULL;
UPDATE polyflow.tx_request_event       SET device.wallet_type = 'injected' WHERE (device).wallet_type IS NULL;
UPDATE polyflow.blockchain_error_event SET device.wallet_type = 'injected' WHERE (device).wallet_type IS NULL;
UPDATE polyflow.error_event            SET device.wallet_type = 'injected' WHERE (device).wallet_type IS NULL;
UPDATE polyflow.user_landed_event      SET device.wallet_type = 'injected' WHERE (device).wallet_type IS NULL;
