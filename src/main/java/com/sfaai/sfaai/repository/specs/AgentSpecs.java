package com.sfaai.sfaai.repository.specs;

import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Agent.AgentStatus;
import com.sfaai.sfaai.entity.Agent.AgentType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Specifications for advanced querying of Agents
 */
public class AgentSpecs {

    /**
     * Specification for agents with the specified client ID
     * @param clientId The client ID to filter by
     * @return Specification for the filter
     */
    public static Specification<Agent> hasClientId(Long clientId) {
        return (root, query, cb) -> {
            if (clientId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("client").get("id"), clientId);
        };
    }

    /**
     * Specification for agents with the specified status
     * @param status The agent status to filter by
     * @return Specification for the filter
     */
    public static Specification<Agent> hasStatus(AgentStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    /**
     * Specification for agents with the specified type
     * @param type The agent type to filter by
     * @return Specification for the filter
     */
    public static Specification<Agent> hasType(AgentType type) {
        return (root, query, cb) -> {
            if (type == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("type"), type);
        };
    }

    /**
     * Specification for agents whose name contains the given text (case insensitive)
     * @param nameFragment The name fragment to search for
     * @return Specification for the filter
     */
    public static Specification<Agent> nameLike(String nameFragment) {
        return (root, query, cb) -> {
            if (nameFragment == null || nameFragment.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + nameFragment.toLowerCase() + "%");
        };
    }

    /**
     * Specification for agents created after the given date
     * @param date The date to filter by
     * @return Specification for the filter
     */
    public static Specification<Agent> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * Specification for agents created before the given date
     * @param date The date to filter by
     * @return Specification for the filter
     */
    public static Specification<Agent> createdBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }
}
