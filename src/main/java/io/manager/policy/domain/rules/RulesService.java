package io.manager.policy.domain.rules;

import io.manager.policy.domain.exception.RiskClassificationException;
import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.RulesResult;
import io.manager.policy.domain.repository.RulesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RulesService implements IRulesService {

    private final RulesRepository rulesRepository;

    @Override
    public RulesResult analyze() {

//        if (policy.getRiskClassification() == null)
//            throw new RiskClassificationException();
//
//        log.debug("Analisando regras para apolice {} - classificacao {}", policy.getId(), policy.getRiskClassification());
//
//        var rules = this.rulesRepository.findByRiskClassification(policy.getRiskClassification())
//                            .stream()
//                            .filter(r -> r.getCategory() == null || r.getCategory().equals(policy.getCategory()))
//                            .filter(r -> policy.getInsuredAmount() <= r.getInsuredAmountLimit())
//                        .toList();
//
//        rules.forEach(r -> log.debug("Apolice validada pela regra {}", r));
//
//        return rules.isEmpty() ? RulesResult.DENY : RulesResult.OK;
        return RulesResult.OK;
    }
}
