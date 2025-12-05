package com.klp.hub.company.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.hub.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompanyTypeTest {

    @Test
    @DisplayName("올바른 회사 타입을 입력하면 CompanyType 으로 변환할 수 있다")
    void validType() {
        String type1 = "SUPPLIER";
        String type2 = "CUSTOMER";

        CompanyType supplierType = CompanyType.from(type1);
        CompanyType customerType = CompanyType.from(type2);

        assertThat(supplierType).isEqualTo(CompanyType.SUPPLIER);
        assertThat(customerType).isEqualTo(CompanyType.CUSTOMER);
    }

    @Test
    @DisplayName("잘못된 회사 타입을 입력하면 예외가 발생한다")
    void invalidType() {
        String invalidType = "InvalidType";

        assertThatThrownBy(() -> CompanyType.from(invalidType))
            .isInstanceOf(BusinessException.class);
    }
}
