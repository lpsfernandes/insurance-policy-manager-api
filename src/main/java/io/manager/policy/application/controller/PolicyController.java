package io.manager.policy.application.controller;

import io.manager.policy.application.controller.dto.PolicyCreatedResponse;
import io.manager.policy.application.controller.dto.PolicyRequest;
import io.manager.policy.application.controller.dto.PolicyResponse;
import io.manager.policy.application.service.ICreatePolicyService;
import io.manager.policy.application.service.ISearchPolicyService;
import io.manager.policy.domain.exception.CreatePolicyException;
import io.manager.policy.domain.exception.PolicyNotFound;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policy")
public class PolicyController {

    private final ICreatePolicyService createPolicyService;
    private final ISearchPolicyService searchPolicyService;

    @Operation(summary = "Registrar apólice de seguro para processamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PolicyCreatedResponse.class))),
            @ApiResponse(responseCode = "500",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)),
                    description = "Ocorreu um erro durante a tentativa de cadastro da apólice")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyCreatedResponse createPolicy(@RequestBody PolicyRequest request) {
        return this.createPolicyService.createPolicy(request)
                    .map(PolicyCreatedResponse::new)
                    .orElseThrow(CreatePolicyException::new);
    }


    @Operation(summary = "Buscar apólice de seguro por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PolicyResponse.class))),
            @ApiResponse(responseCode = "404",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetail.class)),
                    description = "Recurso não encontrado")
    })
    @GetMapping("/{id}")
    public PolicyResponse getPolicy(@RequestParam(name = "id") String policyId) {
        return this.searchPolicyService.getPolicyById(policyId)
                .map(PolicyResponse::new)
                .orElseThrow(() -> new PolicyNotFound(policyId));
    }

    @Operation(summary = "Buscar todas as apólice registradas ou todas as apólices de um client especifico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public Page<PolicyResponse> getPolicies(@RequestParam(name = "clientId", required = false) String clientId,
                            @RequestParam (required = false, defaultValue = "0") Integer page,
                            @RequestParam (required = false, defaultValue = "10") Integer size,
                            @RequestParam (required = false, defaultValue = "createdAt") String sortField,
                            @RequestParam (required = false, defaultValue = "DESC") String sortDirection) {

        var pageable = PageRequest.of(page,
                                      size,
                                      "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                                      sortField);

        return  Optional.ofNullable(clientId)
                .flatMap(id -> Optional.of(this.searchPolicyService.getPolicyByClientId(id, pageable)))
                .or(() ->  Optional.of(this.searchPolicyService.getPolicies(pageable)))
                .map( retPage -> new PageImpl<>(retPage.getContent().stream().map(PolicyResponse::new).toList(),
                        pageable,
                        retPage.getTotalElements()))
                .orElseThrow();


    }
}