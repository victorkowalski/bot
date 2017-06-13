package org.telegram.updateshandlers;

import org.telegram.BotConfig;
import org.telegram.Commands;
import org.telegram.database.DatabaseManager;
import org.telegram.services.DirectionsService;
import org.telegram.services.LocalisationService;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.updateshandlers.SentCallback;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Victor
 */
public class PersonalDataHandlers extends TelegramLongPollingBot {
    private static final String LOGTAG = "PERSONALDATAHANDLERS";

    private static final int WATING_FIRST_NAME_STATUS = 0;
    private static final int WATING_LAST_NAME_STATUS = 1;
    private static final int WATING_PHONE_STATUS = 2;
    private static final int ALL_DATA_RECEIVED_STATUS = 2;

    private final ConcurrentLinkedQueue<Integer> languageMessages = new ConcurrentLinkedQueue<>();


    @Override
    public String getBotUsername() {
        return BotConfig.PERSONAL_DATA_USER;
    }

    @Override
    public String getBotToken() {
        return BotConfig.PERSONAL_DATA_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            handlePersonalData(update);
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    private void handlePersonalData(Update update) throws InvalidObjectException {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            if (languageMessages.contains(message.getFrom().getId())) {
                onLanguageSelected(message);
            } else {
                String language = DatabaseManager.getInstance().getUserLanguage(update.getMessage().getFrom().getId());
                if (message.getText().startsWith(Commands.setLanguageCommand)) {
                    onSetLanguageCommand(message, language);
                } else if (message.getText().startsWith(Commands.startPersonalDataCommand)) {
                    onStartCommand(message, language);
                } else if (!message.getText().startsWith("/")) {
                    if (DatabaseManager.getInstance().getPersonalDataStatus(message.getFrom().getId()) == WATING_FIRST_NAME_STATUS &&
                            message.isReply() &&
                            DatabaseManager.getInstance().getPersonalDataMessageId(message.getFrom().getId()) == message.getReplyToMessage().getMessageId()) {
                        onFirstNameReceived(message, language);

                    } else if (DatabaseManager.getInstance().getPersonalDataStatus(message.getFrom().getId()) == WATING_LAST_NAME_STATUS &&
                            message.isReply() &&
                            DatabaseManager.getInstance().getPersonalDataMessageId(message.getFrom().getId()) == message.getReplyToMessage().getMessageId()) {
                        onLastNameReceived(message, language);
                    } else if (DatabaseManager.getInstance().getPersonalDataStatus(message.getFrom().getId()) == WATING_PHONE_STATUS &&
                            message.isReply() &&
                            DatabaseManager.getInstance().getPersonalDataMessageId(message.getFrom().getId()) == message.getReplyToMessage().getMessageId()) {
                        onPhoneReceived(message, language);
                    }
                }
            }
        }
    }

    private void onFirstNameReceived(Message message, String language) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId());
        sendMessageRequest.setReplyToMessageId(message.getMessageId());
        ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
        forceReplyKeyboard.setSelective(true);
        sendMessageRequest.setReplyMarkup(forceReplyKeyboard);
        sendMessageRequest.setText(LocalisationService.getString("sendLastName", language));

        try {
            sendMessageAsync(sendMessageRequest, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> method, Message sentMessage) {
                    if (sentMessage != null) {
                        DatabaseManager.getInstance().addFirstName(message.getFrom().getId(), WATING_LAST_NAME_STATUS,
                                sentMessage.getMessageId(), message.getText());
                    }
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                }
            });
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }

    }

    private void onLastNameReceived(Message message, String language) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId());
        sendMessageRequest.setReplyToMessageId(message.getMessageId());
        ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
        forceReplyKeyboard.setSelective(true);
        sendMessageRequest.setReplyMarkup(forceReplyKeyboard);
        sendMessageRequest.setText(LocalisationService.getString("sendPhone", language));

        try {
            sendMessageAsync(sendMessageRequest, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> method, Message sentMessage) {
                    if (sentMessage != null) {
                        DatabaseManager.getInstance().addLastName(message.getFrom().getId(), WATING_PHONE_STATUS,
                                sentMessage.getMessageId(), message.getText());
                    }
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                }
            });
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    private void onPhoneReceived(Message message, String language) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId());
        sendMessageRequest.setReplyToMessageId(message.getMessageId());
        ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
        forceReplyKeyboard.setSelective(true);
        sendMessageRequest.setReplyMarkup(forceReplyKeyboard);
        sendMessageRequest.setText(LocalisationService.getString("sendThankYou", language));

        try {
            sendMessageAsync(sendMessageRequest, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> method, Message sentMessage) {
                    if (sentMessage != null) {
                        DatabaseManager.getInstance().addPhone(message.getFrom().getId(), ALL_DATA_RECEIVED_STATUS,
                                sentMessage.getMessageId(), message.getText());
                    }
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                }
            });
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    private void onStartCommand(Message message, String language) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId());
        sendMessageRequest.setReplyToMessageId(message.getMessageId());
        ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
        forceReplyKeyboard.setSelective(true);
        sendMessageRequest.setReplyMarkup(forceReplyKeyboard);
        sendMessageRequest.setText(LocalisationService.getString("initPersonalData", language));

        try {
            sendMessageAsync(sendMessageRequest, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> method, Message sentMessage) {
                    if (sentMessage != null) {
                        DatabaseManager.getInstance().waitFirstName(message.getFrom().getId(), WATING_FIRST_NAME_STATUS,
                                sentMessage.getMessageId());
                    }
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                }
            });
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }

    }

    private void onSetLanguageCommand(Message message, String language) throws InvalidObjectException {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId());
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<LocalisationService.Language> languages = LocalisationService.getSupportedLanguages();
        List<KeyboardRow> commands = new ArrayList<>();
        for (LocalisationService.Language languageItem : languages) {
            KeyboardRow commandRow = new KeyboardRow();
            commandRow.add(languageItem.getCode() + " --> " + languageItem.getName());
            commands.add(commandRow);
        }
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(commands);
        replyKeyboardMarkup.setSelective(true);
        sendMessageRequest.setReplyMarkup(replyKeyboardMarkup);
        sendMessageRequest.setText(LocalisationService.getString("chooselanguage", language));
        try {
            sendMessage(sendMessageRequest);
            languageMessages.add(message.getFrom().getId());
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    private void onLanguageSelected(Message message) throws InvalidObjectException {
        String[] parts = message.getText().split("-->", 2);
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId());
        if (LocalisationService.getLanguageByCode(parts[0].trim()) != null) {
            DatabaseManager.getInstance().putUserLanguage(message.getFrom().getId(), parts[0].trim());
            sendMessageRequest.setText(LocalisationService.getString("languageModified", parts[0].trim()));
        } else {
            sendMessageRequest.setText(LocalisationService.getString("errorLanguage"));
        }
        sendMessageRequest.setReplyToMessageId(message.getMessageId());
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setSelective(true);
        sendMessageRequest.setReplyMarkup(replyKeyboardRemove);
        try {
            sendMessage(sendMessageRequest);
            languageMessages.remove(message.getFrom().getId());
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
