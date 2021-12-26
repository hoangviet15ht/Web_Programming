package com.example.demo.dto.job;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FarmerJobDTO {
    
    @JsonProperty("job_id")
    private Integer jobId;

    @JsonProperty("worker_id")
    private Integer workerId;

    @JsonProperty("deal_price")
    private Float dealPrice;

    @JsonProperty("comment")

    private String comment;

    @JsonProperty("received_at")
    private Date receivedAt;

    @JsonProperty("accepted_at")
    private Date acceptedAt;

    @JsonProperty("status")
    private String status;
}