package com.househost.booking.booking.architecture;

import com.househost.booking.booking.application.service.BookingFormService;
import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.checking.application.service.CheckInService;
import com.househost.booking.checking.application.service.CheckInFinancialResolver;
import com.househost.booking.checking.application.service.CheckInParticipantNotifier;
import com.househost.booking.checkout.application.service.CheckOutService;
import com.househost.booking.checkout.application.service.CheckOutFinancialResolver;
import com.househost.booking.checkout.application.service.CheckOutParticipantNotifier;
import com.househost.booking.checkout.application.service.CheckOutRatingResolver;
import com.househost.publicapi.application.service.PublicBookingService;
import com.househost.ratings.application.port.in.RatingUseCase;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanReplacementUseCase;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrossModuleServiceCommunicationTest {

    @Test
    void principalServicesUseOneParticipantNotifierAndNoResolver() {
        List<Class<?>> principalServiceClassList = List.of(
                BookingService.class,
                CheckInService.class,
                CheckOutService.class,
                PublicBookingService.class
        );

        principalServiceClassList.forEach(principalServiceClass -> {
            assertEquals(
                    1,
                    participantNotifierCount(principalServiceClass),
                    () -> principalServiceClass.getSimpleName()
                            + " deve injetar um unico ParticipantNotifier."
            );
            assertFalse(
                    hasResolver(principalServiceClass),
                    () -> principalServiceClass.getSimpleName()
                            + " nao deve injetar Resolver diretamente."
            );
        });

        assertFalse(
                hasResolver(BookingFormService.class),
                "BookingFormService nao deve usar Resolver para consultas."
        );
    }

    private long participantNotifierCount(Class<?> principalServiceClass) {
        return List.of(principalServiceClass.getDeclaredFields())
                .stream()
                .filter(field -> field.getType().getSimpleName().endsWith("ParticipantNotifier"))
                .count();
    }

    private boolean hasResolver(Class<?> principalServiceClass) {
        return List.of(principalServiceClass.getDeclaredFields())
                .stream()
                .map(Field::getType)
                .anyMatch(fieldType -> fieldType.getSimpleName().endsWith("Resolver"));
    }

    @Test
    void participantNotifiersOwnTheSpecializedResolvers() {
        List<Class<?>> participantNotifierClassList = List.of(
                com.househost.booking.booking.application.service.BookingParticipantNotifier.class,
                com.househost.booking.checking.application.service.CheckInParticipantNotifier.class,
                com.househost.booking.checkout.application.service.CheckOutParticipantNotifier.class,
                com.househost.publicapi.application.service.PublicBookingParticipantNotifier.class
        );

        participantNotifierClassList.forEach(participantNotifierClass ->
                assertFalse(
                        List.of(participantNotifierClass.getDeclaredFields()).isEmpty()
                                || !hasResolver(participantNotifierClass),
                        () -> participantNotifierClass.getSimpleName()
                                + " deve centralizar ao menos um Resolver."
                )
        );
    }

    @Test
    void checkoutRatingMutationFollowsNotifierResolverAndUseCaseFlow() {
        assertTrue(hasFieldOfType(CheckOutService.class, CheckOutParticipantNotifier.class));
        assertTrue(hasFieldOfType(
                CheckOutParticipantNotifier.class,
                CheckOutRatingResolver.class
        ));
        assertTrue(hasFieldOfType(CheckOutRatingResolver.class, RatingUseCase.class));
    }

    @Test
    void stayPaymentMutationFollowsNotifierResolverAndFinancialUseCaseFlow() {
        assertTrue(hasFieldOfType(CheckInService.class, CheckInParticipantNotifier.class));
        assertTrue(hasFieldOfType(
                CheckInParticipantNotifier.class,
                CheckInFinancialResolver.class
        ));
        assertTrue(hasFieldOfType(
                CheckInFinancialResolver.class,
                FinancialTransactionPlanReplacementUseCase.class
        ));
        assertTrue(hasFieldOfType(CheckOutService.class, CheckOutParticipantNotifier.class));
        assertTrue(hasFieldOfType(
                CheckOutParticipantNotifier.class,
                CheckOutFinancialResolver.class
        ));
        assertTrue(hasFieldOfType(
                CheckOutFinancialResolver.class,
                FinancialTransactionPlanReplacementUseCase.class
        ));
    }

    private boolean hasFieldOfType(Class<?> ownerClass, Class<?> fieldClass) {
        return List.of(ownerClass.getDeclaredFields())
                .stream()
                .map(Field::getType)
                .anyMatch(fieldType -> fieldType.equals(fieldClass));
    }
}
