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

//    private void configureSponsorshipMappings(ModelMapper modelMapper) {
//        // Sponsorship to SponsorshipResponseDto
//        TypeMap<Sponsorship, SponsorshipResponseDto> sponsorshipToDto = modelMapper.createTypeMap(
//                Sponsorship.class, SponsorshipResponseDto.class);
//
//        sponsorshipToDto.addMappings(mapper -> {
//            mapper.map(Sponsorship::getId, SponsorshipResponseDto::setId);
//            mapper.using(ctx -> ((Sponsorship) ctx.getSource()).getDonor() != null ?
//                            ((Sponsorship) ctx.getSource()).getDonor().getName() : null)
//                    .map(Sponsorship::getDonor, SponsorshipResponseDto::setDonorName);
//            mapper.using(ctx -> ((Sponsorship) ctx.getSource()).getStudent() != null ?
//                            ((Sponsorship) ctx.getSource()).getStudent().getStudentName() : null)
//                    .map(Sponsorship::getStudent, SponsorshipResponseDto::setStudentName);
//            mapper.using(ctx -> {
//                Sponsorship src = (Sponsorship) ctx.getSource();
//                return src.getStudent() != null && src.getStudent().getInstitution() != null ?
//                        src.getStudent().getInstitution().getInstitutionName() : null;
//            }).map(Sponsorship::getStudent, SponsorshipResponseDto::setInstitutionName);
//            mapper.using(ctx -> {
//                List<Payment> payments = ((Sponsorship) ctx.getSource()).getPayments();
//                return payments != null ? payments.size() : 0;
//            }).map(Sponsorship::getPayments, SponsorshipResponseDto::setTotalPayments);
//            mapper.map(Sponsorship::getMonthlyAmount, SponsorshipResponseDto::setMonthlyAmount);
//            mapper.map(Sponsorship::getTotalAmount, SponsorshipResponseDto::setTotalAmount);
//            mapper.map(Sponsorship::getStartDate, SponsorshipResponseDto::setStartDate);
//            mapper.map(Sponsorship::getEndDate, SponsorshipResponseDto::setEndDate);
//            mapper.map(Sponsorship::getTotalMonths, SponsorshipResponseDto::setTotalMonths);
//            mapper.map(Sponsorship::getPaymentMethod, SponsorshipResponseDto::setPaymentMethod);
//            mapper.map(Sponsorship::getStatus, SponsorshipResponseDto::setStatus);
//            mapper.map(Sponsorship::getTotalPaidAmount, SponsorshipResponseDto::setTotalPaidAmount);
//            mapper.map(Sponsorship::getPaidUpTo, SponsorshipResponseDto::setPaidUpTo);
//            mapper.map(Sponsorship::getLastPaymentDate, SponsorshipResponseDto::setLastPaymentDate);
//            mapper.map(Sponsorship::isPaymentDue, SponsorshipResponseDto::setPaymentDue);
//            mapper.map(Sponsorship::isOverdue, SponsorshipResponseDto::setOverdue);
//            mapper.map(Sponsorship::getNextPaymentDueDate, SponsorshipResponseDto::setNextPaymentDueDate);
//            mapper.map(Sponsorship::getMonthsPaid, SponsorshipResponseDto::setMonthsPaid);
//            mapper.map(Sponsorship::getMonthsRemaining, SponsorshipResponseDto::setMonthsRemaining);
//        });
//
//        // SponsorshipRequestDto to Sponsorship
//        TypeMap<SponsorshipRequestDto, Sponsorship> dtoToSponsorship = modelMapper.createTypeMap(
//                SponsorshipRequestDto.class, Sponsorship.class);
//
//        dtoToSponsorship.addMappings(mapper -> {
//            mapper.skip(Sponsorship::setId);
//            mapper.skip(Sponsorship::setStatus);
//            mapper.skip(Sponsorship::setPayments);
//            mapper.skip(Sponsorship::setPaidUpTo);
//            mapper.skip(Sponsorship::setLastPaymentDate);
//            mapper.skip(Sponsorship::setTotalAmount);
//            mapper.skip(Sponsorship::setTotalMonths);
//            mapper.skip(Sponsorship::setTotalPaidAmount);
//            mapper.skip(Sponsorship::setDonor);
//            mapper.skip(Sponsorship::setStudent);
//            mapper.skip(Sponsorship::setMonthlyAmount);
//            mapper.map(SponsorshipRequestDto::getPaymentMethod, Sponsorship::setPaymentMethod);
//            mapper.map(SponsorshipRequestDto::getMonthlyAmount, Sponsorship::setMonthlyAmount);
//        });
//
//        dtoToSponsorship.setPostConverter(context -> {
//            Sponsorship sponsorship = context.getDestination();
//            SponsorshipRequestDto dto = context.getSource();
//
//            if (dto.getStartDate() != null && dto.getEndDate() != null) {
//                sponsorship.setDateRange(dto.getStartDate(), dto.getEndDate());
//            }
//
//            // Initialize payments list if null
//            if (sponsorship.getPayments() == null) {
//                sponsorship.setPayments(Collections.emptyList());
//            }
//
//            return sponsorship;
//        });
//    }
//
//    private void configurePaymentMappings(ModelMapper modelMapper) {
//        // Payment to PaymentResponseDto
//        TypeMap<Payment, PaymentResponseDto> paymentToDto = modelMapper.createTypeMap(
//                Payment.class, PaymentResponseDto.class);
//
//        paymentToDto.addMappings(mapper -> {
//            mapper.map(Payment::getId, PaymentResponseDto::setId);
//            mapper.using(ctx -> ((Payment) ctx.getSource()).getSponsorship() != null ?
//                            ((Payment) ctx.getSource()).getSponsorship().getId() : null)
//                    .map(Payment::getSponsorship, PaymentResponseDto::setSponsorshipId);
//            mapper.using(ctx -> {
//                Payment src = (Payment) ctx.getSource();
//                return src.getSponsorship() != null && src.getSponsorship().getDonor() != null ?
//                        src.getSponsorship().getDonor().getName() : null;
//            }).map(Payment::getSponsorship, PaymentResponseDto::setDonorName);
//            mapper.using(ctx -> {
//                Payment src = (Payment) ctx.getSource();
//                return src.getSponsorship() != null && src.getSponsorship().getStudent() != null ?
//                        src.getSponsorship().getStudent().getStudentName() : null;
//            }).map(Payment::getSponsorship, PaymentResponseDto::setStudentName);
//            mapper.using(ctx -> {
//                Payment src = (Payment) ctx.getSource();
//                return src.getSponsorship() != null &&
//                        src.getSponsorship().getStudent() != null &&
//                        src.getSponsorship().getStudent().getInstitution() != null ?
//                        src.getSponsorship().getStudent().getInstitution().getInstitutionName() : null;
//            }).map(Payment::getSponsorship, PaymentResponseDto::setInstitutionName);
//            mapper.map(Payment::getPaymentDate, PaymentResponseDto::setPaymentDate);
//            mapper.map(Payment::getPaidUpTo, PaymentResponseDto::setPaidUpTo);
//            mapper.map(Payment::getAmount, PaymentResponseDto::setAmount);
//            mapper.map(Payment::getPaymentMethod, PaymentResponseDto::setPaymentMethod);
//            mapper.map(Payment::getStatus, PaymentResponseDto::setStatus);
//            mapper.map(Payment::getStartDate, PaymentResponseDto::setStartDate);
//            mapper.map(Payment::getEndDate, PaymentResponseDto::setEndDate);
//            mapper.map(Payment::getTotalMonths, PaymentResponseDto::setTotalMonths);
//            mapper.map(Payment::getTotalAmount, PaymentResponseDto::setTotalAmount);
//        });
//
//        // PaymentRequestDto to Payment
//        TypeMap<PaymentRequestDto, Payment> dtoToPayment = modelMapper.createTypeMap(
//                PaymentRequestDto.class, Payment.class);
//
//        dtoToPayment.addMappings(mapper -> {
//            mapper.skip(Payment::setId);
//            mapper.skip(Payment::setPaymentDate);
//            mapper.skip(Payment::setStatus);
//            mapper.skip(Payment::setSponsorship);
//            mapper.skip(Payment::setStartDate);
//            mapper.skip(Payment::setEndDate);
//            mapper.skip(Payment::setTotalMonths);
//            mapper.skip(Payment::setTotalAmount);
//            mapper.map(PaymentRequestDto::getPaymentMethod, Payment::setPaymentMethod);
//            mapper.map(PaymentRequestDto::getAmount, Payment::setAmount);
//            mapper.map(PaymentRequestDto::getPaidUpTo, Payment::setPaidUpTo);
//        });
//
//        dtoToPayment.setPostConverter(context -> {
//            Payment payment = context.getDestination();
//            payment.setPaymentDate(LocalDate.now());
//            payment.setStatus(PaymentStatus.COMPLETED);
//            return payment;
//        });
//    }
}