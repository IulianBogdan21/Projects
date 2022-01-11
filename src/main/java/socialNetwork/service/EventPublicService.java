package socialNetwork.service;

import socialNetwork.domain.models.DTOEventPublicUser;
import socialNetwork.domain.models.EventNotification;
import socialNetwork.domain.models.EventPublic;
import socialNetwork.domain.validators.EventPublicValidator;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class EventPublicService {
    PagingRepository<Long, EventPublic> eventPublicPagingRepository;
    PagingRepository<UnorderedPair<Long,Long>, DTOEventPublicUser> eventPublicUserPagingRepository;
    EventPublicValidator eventPublicValidator;

    public EventPublicService(PagingRepository<Long, EventPublic> eventPublicPagingRepository,
                              PagingRepository<UnorderedPair<Long, Long>, DTOEventPublicUser> eventPublicUserPagingRepository,
                              EventPublicValidator eventPublicValidator) {
        this.eventPublicPagingRepository = eventPublicPagingRepository;
        this.eventPublicUserPagingRepository = eventPublicUserPagingRepository;
        this.eventPublicValidator = eventPublicValidator;
    }

    public Optional<EventPublic> addEventPublicService(String name, String description, LocalDateTime date){
        EventPublic eventPublicToSave = new EventPublic(name,description,date);
        eventPublicValidator.validate(eventPublicToSave);
        Optional<EventPublic> eventPublicSave = eventPublicPagingRepository.save(eventPublicToSave);
        return eventPublicSave;
    }

    public Optional<DTOEventPublicUser> subscribeEventPublicService(Long idUser, Long idEventPublic){
        DTOEventPublicUser dtoEventPublicUserToSave = new DTOEventPublicUser(idUser,idEventPublic);
        Optional<DTOEventPublicUser> dtoEventPublicUserSave = eventPublicUserPagingRepository
                .save(dtoEventPublicUserToSave);
        return dtoEventPublicUserSave;
    }

    public Optional<DTOEventPublicUser> stopNotificationEventPublicService(Long idUser,Long idEventPublic){
        DTOEventPublicUser dtoEventPublicUserToUpdate = new DTOEventPublicUser
                (idUser,idEventPublic, EventNotification.REJECT);
        Optional<DTOEventPublicUser> dtoEventPublicUserUpdate = eventPublicUserPagingRepository
                .update(dtoEventPublicUserToUpdate);
        return dtoEventPublicUserUpdate;
    }

    /*
    return all events for which the user is subscribed and accept notifications from them
    Also return that event whose deadline is less than a given period of time
     */
    public List<EventPublic> filterAllEventPublicForNotificationService(Long idUser, Long days){
        LocalDateTime thisMoment = LocalDateTime.now();
        Predicate<DTOEventPublicUser> predicate = dtoEventPublicUser -> {
            return dtoEventPublicUser.getIdUser().equals(idUser) &&
                    dtoEventPublicUser.getReceivedNotification().equals(EventNotification.APPROVE);
        };

        List<EventPublic> eventPublicList = eventPublicUserPagingRepository.getAll()
                .stream()
                .filter(predicate)
                .map(dtoEventPublicUser -> {
                    Long idEventPublic = dtoEventPublicUser.getIdEventPublic();
                    EventPublic eventPublic = eventPublicPagingRepository.find(idEventPublic).get();
                    return eventPublic;
                })
                .filter(eventPublic -> {
                    LocalDateTime dateOfEvent = eventPublic.getDate();
                    Long years = ChronoUnit.YEARS.between(dateOfEvent,thisMoment);
                    Long months = ChronoUnit.MONTHS.between(dateOfEvent,thisMoment);
                    Long daysUntil = ChronoUnit.DAYS.between(dateOfEvent,thisMoment);
                    daysUntil += months*30 + years*365;
                    return  ( daysUntil.compareTo(days) <= 0 );
                })
                .toList();
        return eventPublicList;
    }


}
