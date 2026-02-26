package com.op1m.medrem.backend_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramUserDTO {
    private Long id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String username;

    @JsonProperty("photo_url")
    private String photoUrl;

    @JsonProperty("auth_date")
    private String authDate;

    @JsonProperty("language_code")
    private String languageCode;
}