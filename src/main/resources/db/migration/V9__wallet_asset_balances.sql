CREATE TYPE polyflow.ASSET_BALANCE AS (
    chain_id BIGINT,
    amount   NUMERIC(78)
);

CREATE TYPE polyflow.FUNGIBLE_TOKEN_BALANCE AS (
    token_address VARCHAR,
    chain_id      BIGINT,
    amount        NUMERIC(78)
);

CREATE TYPE polyflow.NFT_TOKEN_BALANCE AS (
    token_address VARCHAR,
    chain_id      BIGINT,
    owns_asset    BOOLEAN,
    owned_assets  NUMERIC(78)[]
);

CREATE TYPE polyflow.ASSET_RPC_CALL AS (
    token_address VARCHAR,
    chain_id      BIGINT,
    is_nft        BOOLEAN
);

CREATE TABLE polyflow.wallet_portfolio_data (
    wallet_address          VARCHAR                  NOT NULL PRIMARY KEY,
    native_asset_balances   ASSET_BALANCE[]          NOT NULL,
    fungible_token_balances FUNGIBLE_TOKEN_BALANCE[] NOT NULL,
    nft_token_balances      NFT_TOKEN_BALANCE[]      NOT NULL,
    failed_rpc_calls        ASSET_RPC_CALL[]         NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE polyflow.native_asset_usd_value (
    chain_id   BIGINT                   NOT NULL PRIMARY KEY,
    usd_value  NUMERIC                  NOT NULL,
    decimals   INTEGER                  NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE polyflow.fungible_token_usd_value (
    token_address VARCHAR                  NOT NULL,
    chain_id      BIGINT                   NOT NULL,
    usd_value     NUMERIC                  NOT NULL,
    decimals      INTEGER                  NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (token_address, chain_id)
);

CREATE TABLE polyflow.nft_token_usd_value (
    token_address VARCHAR                  NOT NULL,
    token_id      NUMERIC(78)              NOT NULL,
    chain_id      BIGINT                   NOT NULL,
    usd_value     NUMERIC                  NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (token_address, token_id, chain_id)
);
