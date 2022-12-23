package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.Buttons.ReplyKeyboards;
import pro.sky.telegrambot.constants.AdminMenuItems;
import pro.sky.telegrambot.constants.MenuItemsNames;

import pro.sky.telegrambot.constants.Strings;
import pro.sky.telegrambot.model.AdminResponses;
import pro.sky.telegrambot.model.Responses;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pro.sky.telegrambot.constants.Strings.*;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final ShelterService shelterService;
    private final ReplyKeyboards keyboards;

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    @Autowired
    private TelegramBot telegramBot;
    private Boolean recordChange = false;
    private Boolean status = false;


    private Map<Long, Responses> pendingResponses;
    private Map<Long, AdminResponses> adminPendingResponses;
    private final static Long volunteerChatId = 202671625L;

    public TelegramBotUpdatesListener(ReplyKeyboards keyboards, ShelterService shelterService) {
        this.keyboards = keyboards;
        this.shelterService = shelterService;
        pendingResponses = new HashMap<Long, Responses>();
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            try {
                long chatId = update.message().chat().id();
                String messageText = update.message().text();
                if (chatId == volunteerChatId) {
                    if (pendingResponses.containsKey(chatId)) {
                        adminRequestProccessing(update.message());
                    } else {
                        adminMenu(chatId, messageText);
                    }
                } else {
                    if (pendingResponses.containsKey(chatId)) {
                        requestProccessing(update.message());
                    } else {
                        sendMenuAndReplies(chatId, messageText);
                    }

                }
            } catch (Exception e) {

            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void requestProccessing(Message message) {
        Long chatid = message.chat().id();
        switch (pendingResponses.get(chatid)) {
            case REPORT:
                shelterService.getReport(message);
                break;
            case REQUEST:
                shelterService.getRequest(message);
                break;
            case CONTACT:
                shelterService.getContact(message);
                break;
        }
        pendingResponses.remove(chatid);

    }

    private void sendMenuAndReplies(long chatId, String command) {
        String replyTextMessage;
        SendMessage message;

        switch (command) {
            // главное меню и общее
            case MenuItemsNames.START:
            case MenuItemsNames.TO_MAIN_MENU:
                message = new SendMessage(chatId, WELCOME_MESSAGE_MENU_MAIN);
                message.replyMarkup(keyboards.mainMenuKeyboards);
                break;
            case MenuItemsNames.TO_INFO_ABOUT_SHELTER:
                message = new SendMessage(chatId, WELCOME_MESSAGE_MENU_ABOUT_SHELTER);
                message.replyMarkup(keyboards.aboutShelterMenuKeyboards);
                break;
            case MenuItemsNames.TO_ADOPT_DOG:
                message = new SendMessage(chatId, WELCOME_MESSAGE_MENU_ADOPT_DOG);
                message.replyMarkup(keyboards.adoptDogMenuKeyboards);
                break;
            case MenuItemsNames.SEND_REPORT:
                message = new SendMessage(chatId, SEND_REPORT_OFFER);
                pendingResponses.put(chatId, Responses.REPORT);
                break;

            case MenuItemsNames.CALL_VOLUNTEER:

                //callVolunteer(inputMessage);
                message = new SendMessage(chatId, WELCOME_MESSAGE_FOUR);
                message.replyMarkup(keyboards.emptyKeyboard);
                pendingResponses.put(chatId, Responses.REQUEST);
                break;
            case MenuItemsNames.SEND_CONTACTS:
                pendingResponses.put(chatId, Responses.CONTACT);
                message = new SendMessage(chatId, RECORD_CONTACT);
                message.replyMarkup(keyboards.emptyKeyboard);
                //message = new SendMessage(chatId, RECORD_CONTACT);
                //recordStatus = true;
                break;
            // меню один
            case MenuItemsNames.ABOUT_SHELTER_INFO:
                replyTextMessage = shelterService.getAbout();
                message = new SendMessage(chatId, replyTextMessage);
                break;
            case MenuItemsNames.ABOUT_SHELTER_ADDRESS_SCHEDULE:
                replyTextMessage = shelterService.getScheduleAndAdress();
                message = new SendMessage(chatId, replyTextMessage);
                break;
            case MenuItemsNames.ABOUT_SHELTER_SAFETY_PRECUATUINS:
                replyTextMessage = shelterService.getSafetyPrecautions();
                message = new SendMessage(chatId, replyTextMessage);
                break;

            // меню два
            case MenuItemsNames.ADOPT_DOG_MEETING_RULES:
                replyTextMessage = shelterService.getmeetingRules();
                message = new SendMessage(chatId, replyTextMessage);
                break;
            case MenuItemsNames.ADOPT_DOG_DOCUMENTS:
                replyTextMessage = shelterService.getDocumentsForAdpotion();
                message = new SendMessage(chatId, replyTextMessage);
                break;
            case MenuItemsNames.ADOPT_DOG_RECOMENDATIONS:
                message = new SendMessage(chatId, RECOMMENDATIONS_MENU_GREETINGS);
                message.replyMarkup(keyboards.recommendationMenuKeyboard);
                break;
            case MenuItemsNames.RECOMMENDATIONS_TRANSPORTATION:
                replyTextMessage = shelterService.getTransportationRecommendations();
                message = new SendMessage(chatId, replyTextMessage);
                break;
            case MenuItemsNames.RECOMMENDATIONS_HOME_IMPROVEMENT_FOR_ADUALTS:
                replyTextMessage = shelterService.getHomeImprovementsForAdultsRecommendations();
                message = new SendMessage(chatId, replyTextMessage);
                break;
            case MenuItemsNames.RECOMMENDATIONS_HOME_IMPROVEMENT_FOR_PUPPIES:
                replyTextMessage = shelterService.getHomeImprovementsForPuppiesRecommendations();
                message = new SendMessage(chatId, replyTextMessage);
                break;
            case MenuItemsNames.RECOMMENDATIONS_HOME_IMPROVEMENT_FOR_DISABLED:
                replyTextMessage = shelterService.getHomeImprovementsForDisabledRecommendations();
                message = new SendMessage(chatId, replyTextMessage);
                break;


            case MenuItemsNames.ADOPT_DOG_APPROVED_CYNOLOGYSTS:
                replyTextMessage = shelterService.getApprovedCynologysts();
                message = new SendMessage(chatId, replyTextMessage);
                break;
            case MenuItemsNames.RECOMMENDATIONS_CYNOLOGYSTS_ADVICES:
                replyTextMessage = shelterService.getCynologystsAdvices();
                message = new SendMessage(chatId, replyTextMessage);
                break;

            case MenuItemsNames.ADOPT_DOG_DECLINE_REASONS:
                replyTextMessage = shelterService.getDeclineReasons();
                message = new SendMessage(chatId, replyTextMessage);
                break;

            default:
                message = new SendMessage(chatId, SORRY_MESSAGE);
        }
        telegramBot.execute(message);
    }

    private void adminRequestProccessing(Message message) {
        Long chatid = message.chat().id();
        String inputText = message.text();
        switch (adminPendingResponses.get(volunteerChatId)) {
            case DELETE:
                shelterService.deleteContact(inputText);
                break;
            case APPOINT_GUARDIAN:
                shelterService.updateContact(inputText);
                break;
            case CREATE:
                shelterService.addContact(inputText);
                break;
            case EXTEND_PROBATION:
                shelterService.extendProbation(inputText);
                break;
            case PRINT_CONTACT_LIST:
                shelterService.printContactsList();
        }
        adminPendingResponses.remove(chatid);

    }


    private void adminMenu(long chatId, String command) {
        SendMessage message;
        switch (command) {
            case "/start":
            case AdminMenuItems.TO_MAIN_MENU:
                message = new SendMessage(volunteerChatId, Strings.ADMIN_MAIN_MENU);
                message.replyMarkup(keyboards.сontrolMainMenu);
                break;
            case AdminMenuItems.TO_CONTACTS_MENU:
                message = new SendMessage(volunteerChatId, Strings.CONTACTS_MENU);
                message.replyMarkup(keyboards.contactsControlMenu);
                break;
            case AdminMenuItems.DELETE_CONTACT:
                adminPendingResponses.put(volunteerChatId, AdminResponses.DELETE);
                message = new SendMessage(volunteerChatId, Strings.DELETE_CONTACT);
                break;
            case AdminMenuItems.APPOINT_GUARDIAN:
                adminPendingResponses.put(volunteerChatId, AdminResponses.APPOINT_GUARDIAN);
                message = new SendMessage(volunteerChatId, Strings.APPOINT_GUARDIAN);
                break;
            case AdminMenuItems.EXTEND_PROBATION:
                adminPendingResponses.put(volunteerChatId, AdminResponses.EXTEND_PROBATION);
                message = new SendMessage(volunteerChatId, Strings.EXTEND_PROBATION);
                break;
            case AdminMenuItems.ADD_CONTACT:
                adminPendingResponses.put(volunteerChatId, AdminResponses.CREATE);
                message = new SendMessage(volunteerChatId, Strings.ADD_CONTACT);
                break;
            case AdminMenuItems.TO_REPORTS_MENU:
                message = new SendMessage(volunteerChatId, Strings.REPORTS_MENU);
                break;
            case AdminMenuItems.PRINT_CONTACTS_LIST:
                adminPendingResponses.put(volunteerChatId, AdminResponses.PRINT_CONTACT_LIST);
                message = new SendMessage(volunteerChatId, Strings.PRINT_CONTACTS_LIST);
                break;
            //        case AdminMenuItems.WATCH_IRRESPONSIBLES:   // писать
            //      text = personRepository.getPersonFromDataBase(status).toString();
            //message = new SendMessage(volunteerChatId, text);
            //       break;
            default:
                message = new SendMessage(volunteerChatId, "Ошибка ввода данных. Попробуйте снова и внимательно!");
                break;
        }
        telegramBot.execute(message);

    }
/*
    private SendMessage createControlMessage(String messageText) {
        String text = "Выберете одно из действий";
        SendMessage message = null;
        switch (messageText) {
            // главное меню
            case "/control":
            case "Вернуться назад":
                message = new SendMessage(volunteerChatId, text);
                message.replyMarkup(keyboards.replyControlMainKeyboards);
                break;
            case "Контакты":
                status = false;
                message = new SendMessage(volunteerChatId, "Контакты. " + text);
                message.replyMarkup(keyboards.contactsControlMenu);
                break;
            case "Усыновители":
                status = true;
                message = new SendMessage(volunteerChatId, "Усыновители. " + text);
                message.replyMarkup(keyboards.contactsControlMenu);
                break;
            case "Испытательный срок":
                message = new SendMessage(volunteerChatId, "Испытательный срок. " + text);
                message.replyMarkup(keyboards.replyControlTwoKeyboards);
                break;
            case "Отчёты":
                text = "Чтобы посмотреть отчёты усыновителя введите его ID.\n" +
                        "Вы модете посмотреть ID в списке усыновителей.\n" + "Например: 156";
                message = new SendMessage(volunteerChatId, text);
                //recordChange = true;
                break;
            // меню один
            case "Посмотреть":

                List<Person> people = personRepository.getPersonFromDataBase(status);

                text = people.toString();
                message = new SendMessage(volunteerChatId, text);

                break;
            case "Добавить":
                if (!status) {
                    text = "Чтобы добавить контакт введите через пробел имя пользователя в Телеграмме, имя, номер телефона, фамилию.\n" +
                            "Например: CrazyPuck 898745212350 Иван Иванов";
                } else {
                    text = "Чтобы добавить усыновителя введите через пробел его ID, дату начала и дату окончания испытания.\n" +
                            "Вы модете посмотреть ID в списке контактов.\n" + "Например: 156 2022-10-01 2022-11-01";
                }
                message = new SendMessage(volunteerChatId, text);
                recordChange = true;
                break;
            case "Удалить":
                if (!status) {
                    text = "Для удаления контакта из списка, введите его ID, его вы можете посмотреть в Списке контактов";
                } else {
                    text = "Для удаления усыновителя из списка, введите его ID, его вы можете посмотреть в Списке усыновителей";
                }
                message = new SendMessage(volunteerChatId, text);
                recordChange = true;
                break;
            // меню два
            case "Продлить срок":
                text = "Чтобы продлить испытательный срок введите через пробел ID нарушителя и дату окончания испытания.\n" +
                        "Вы модете посмотреть ID в списке контактов.\n" + "Например: 156 2022-12-01";
                message = new SendMessage(volunteerChatId, text);
                recordChange = true;
                break;
            case "Список должников":   // писать
                //text = personRepository.getPersonFromDataBase(status).toString();
                //message = new SendMessage(volunteerChatId, text);
                break;
            default:
                message = new SendMessage(volunteerChatId, "Моя, твоя, не понимать");
        }
        return message;
    }

    private String[] parseString(String insertText) {
        return insertText.split(" ");
    }
*/
}
