package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.CatShelter;
import pro.sky.telegrambot.repository.PersonRepository;
import pro.sky.telegrambot.repository.ReportRepository;

@Service
public class CatShelterService extends ShelterService{
    public CatShelterService(CatShelter catShelter, PersonRepository contactRepository, ReportRepository reportRepository, TelegramBot telegramBot) {
        super(catShelter, contactRepository, reportRepository, telegramBot);
    }
}
