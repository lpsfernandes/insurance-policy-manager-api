package io.manager.policy.api.controller;

import io.manager.policy.api.dto.PolicyRequestRecord;
import io.manager.policy.api.dto.PolicyResponseRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/policy")
public class PolicyController {

    @PostMapping
    public String createPolicy(@RequestBody PolicyRequestRecord request) {
        return "";
    }

    @GetMapping
    public PolicyResponseRecord getPolicy(@RequestParam(name = "policyId", required = false) String policyId) {
        return null;
    }
}