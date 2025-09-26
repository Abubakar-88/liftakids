package org.liftakids.config;

import org.liftakids.dto.sponsorship.PaymentRequestDto;
import org.liftakids.dto.sponsorship.PaymentResponseDto;
import org.liftakids.dto.sponsorship.SponsorshipRequestDto;
import org.liftakids.dto.sponsorship.SponsorshipResponseDto;
import org.liftakids.entity.*;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Configuration
public class AppConfig {
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
// Configure Sponsorship mapping
        modelMapper.typeMap(SponsorshipRequestDto.class, Sponsorship.class)
                .addMappings(mapper -> {
                    mapper.skip(Sponsorship::setId);
                    mapper.skip(Sponsorship::setStatus);
                    mapper.skip(Sponsorship::setPayments);
                    mapper.skip(Sponsorship::setPaidUpTo);
                    mapper.skip(Sponsorship::setLastPaymentDate);
                    mapper.skip(Sponsorship::setTotalAmount);
                    mapper.skip(Sponsorship::setTotalMonths);
                    mapper.skip(Sponsorship::setTotalPaidAmount);
                    mapper.skip(Sponsorship::setDonor);
                    mapper.skip(Sponsorship::setStudent);
                    mapper.skip(Sponsorship::setMonthlyAmount);
                });

        return modelMapper;

    }


}