package pro.sky.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.Person;
import pro.sky.telegrambot.repository.PersonRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Сервис для работы с сущностью Person
 */

public  class PersonService {

    private final PersonRepository personRepository;
    private final Logger logger = LoggerFactory.getLogger(PersonService.class);

    PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }



    /**
     * Возвращает список Person
     *
     * @return
     */
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    /**
     * Возвращает Person по id
     *
     * @param id
     * @return
     */
    public Person getPerson(Long id) {

        return (Person) personRepository.findById(Long.valueOf(id)).orElseThrow();
    }

    /**
     * Создаёт новую запись Person в БД
     *
     * @param person
     * @return
     */
    public void savePerson(Person person) {
        personRepository.save(person);
    }

    /**
     * Редактирует запись Person в БД
     *
     * @param person
     * @return
     */
    public void editPerson(Person person) {
        personRepository.save(person);
    }

    /**
     * Удаляет запись из БД по id
     *
     * @param id
     */
    public void deletePerson(Long id) {
        try {
            personRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        logger.error("There is not animal with id = " + id);
        personRepository.deleteById(id);
    }


    public boolean failProbation(String id) {
        Person failedPerson;
        try {
            failedPerson = (Person) personRepository.findById(Long.valueOf(id)).orElseThrow();
            failedPerson.setAdoptive(false);
            failedPerson.setEndProbationDate(null);
            failedPerson.setStartProbationDate(null);
            personRepository.save(failedPerson);
            return true;
        }catch (Exception e){
            logger.error("не нашелся контакт");
        }
        return false;
    }
}
