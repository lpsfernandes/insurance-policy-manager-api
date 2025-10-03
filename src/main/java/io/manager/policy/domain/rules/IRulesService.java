package io.manager.policy.domain.rules;

import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.RulesResult;

public interface IRulesService {
    RulesResult analyze();
}
