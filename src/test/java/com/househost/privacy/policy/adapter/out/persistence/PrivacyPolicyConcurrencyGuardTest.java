package com.househost.privacy.policy.adapter.out.persistence;

import com.househost.privacy.policy.adapter.out.persistence.entity.PrivacyPolicyJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.LockModeType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivacyPolicyConcurrencyGuardTest {
    @Test
    void combinesDatabaseUniquenessWithPessimisticPublicationLock() throws Exception {
        Field currentSlot = PrivacyPolicyJpaEntity.class.getDeclaredField("currentSlot");
        Column currentSlotColumn = currentSlot.getAnnotation(Column.class);
        Method lockedLookup = PrivacyPolicyJpaRepository.class.getDeclaredMethod(
                "findWithLockByCurrentSlot", String.class
        );

        assertTrue(currentSlotColumn.unique());
        assertEquals("current_slot", currentSlotColumn.name());
        assertEquals(
                LockModeType.PESSIMISTIC_WRITE,
                lockedLookup.getAnnotation(Lock.class).value()
        );
    }
}
