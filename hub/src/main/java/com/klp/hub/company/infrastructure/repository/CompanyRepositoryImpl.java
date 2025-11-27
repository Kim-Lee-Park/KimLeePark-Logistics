package com.klp.hub.company.infrastructure.repository;

import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.QCompany;
import com.klp.hub.company.domain.repository.CompanyRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyJpaRepository companyJpaRepository;
    private final JPAQueryFactory queryFactory;

    private QCompany qCompany = QCompany.company;

    @Override
    public Optional<Company> findById(UUID companyId) {
        return companyJpaRepository.findById(companyId);
    }

    @Override
    public List<Company> findAllByName(String name) {
        return queryFactory
            .selectFrom(qCompany)
            .where(
                isNotDeleted(),
                nameEqual(name)
            )
            .fetch();
    }

    private BooleanExpression nameEqual(String name) {
        if (name == null) {
            return null;
        }

        return qCompany.name.eq(name);
    }

    private BooleanExpression isNotDeleted() {
        return qCompany.deletedAt.isNull();
    }
}
