DROP TABLE polyflow.nft_token_usd_value;

CREATE TABLE polyflow.nft_token_eth_value (
    token_address VARCHAR                  NOT NULL,
    chain_id      BIGINT                   NOT NULL,
    eth_value     NUMERIC                  NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (token_address, chain_id)
);
