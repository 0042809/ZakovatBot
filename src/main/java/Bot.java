import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;

public class Bot extends TelegramLongPollingBot {
    private Map<Long, Boolean> userAppealState = new HashMap<>();
    private final long groupChatId = -4598280558L;
    private static final String BOT_TOKEN = "8050910760:AAHv8TPNkAgKabFw1tEYjTz1rpkZyQ7KpCI";

    public Bot() {
        super(BOT_TOKEN);
    }

    @Override
    public String getBotUsername() {
        return "https://t.me/iiauappelyatsiyabot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            System.out.println("Callback Query Detected: " + update.getCallbackQuery().getData());
            handleCallbackQuery(update);
        }
    }

    private void handleTextMessage(Update update) {
        Long userId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        if (userAppealState.getOrDefault(userId, false)) {
            processAppeal(update);
        } else if (messageText.equals("/appelyatsiya")) {
            handleAppelyatsiya(update);
        } else if (messageText.equals("/savollar_bazasi")) {
            handleSavollarBazasi(update);
        } else if (messageText.equals("/results")) {
            handleResults(update);
        } else if (messageText.equals("/start")) {
            sendWelcomeMessage(userId);
        } else {
            sendUnknownCommandMessage(userId);
        }
    }

    private void processAppeal(Update update) {
        Long userId = update.getMessage().getChatId();
        String userMessage = update.getMessage().getText();

        SendMessage forwardMessage = new SendMessage();
        forwardMessage.setChatId(groupChatId);
        forwardMessage.setText(userMessage);

        try {
            execute(forwardMessage);
            sendMessage(userId, "Rahmat! Sizning appelyatsiyangiz qabul qilindi.");
            userAppealState.put(userId, false);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleAppelyatsiya(Update update) {
        Long userId = update.getMessage().getChatId();

        if (!userAppealState.getOrDefault(userId, false)) {
            SendMessage guideMessage = new SendMessage();
            guideMessage.setChatId(userId);
            guideMessage.setText("""
                    Appelyatsiya berish uchun quyidagi amallarni bajaring:
                    1. Guruhingizni nomini yozing;
                    2. Savol tartib raqamini yozing;
                    3. Javob varaqasiga yozib bergan javobingizni yozing;
                    4. Boshlovchi e'lon qilgan javobni yozing;
                    5. Javobingizni qabul qilish uchun manba ko'rsating;
                    6. Agar savolni yechilishi uchun appelyatsiya bermoqchi bo'lsangiz, xatolik haqida xabar bering.
                    """);

            try {
                execute(guideMessage);
                userAppealState.put(userId, true);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleResults(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Please select the round:");

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("1-tur");
        button1.setCallbackData("1-tur");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("2-tur");
        button2.setCallbackData("2-tur");

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("3-tur");
        button3.setCallbackData("3-tur");

        InlineKeyboardButton button4 = new InlineKeyboardButton(); // Corrected button4
        button4.setText("4-tur");
        button4.setCallbackData("4-tur");

        InlineKeyboardButton totalButton = new InlineKeyboardButton();
        totalButton.setText("Total");
        totalButton.setCallbackData("Total");

        // Arrange buttons in rows
        List<List<InlineKeyboardButton>> keyboard = Arrays.asList(
                Arrays.asList(button1, button2),
                Arrays.asList(button3, button4),
                Arrays.asList(totalButton)
        );

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        System.out.println("Callback Data: " + callbackData);

        switch (callbackData) {
            case "1-tur":
                sendRoundImage(chatId, "src/main/resources/images/img_1.png");
                break;
            case "2-tur":
                sendRoundImage(chatId, "src/main/resources/images/img_2.png");
                break;
            case "3-tur":
                sendRoundImage(chatId, "src/main/resources/images/img_3.png");
                break;
            case "4-tur":
                sendRoundImage(chatId, "src/main/resources/images/img_4.png");
                break;
            case "Total":
                sendRoundImage(chatId, "src/main/resources/images/img_total.png");
                sendRoundImage(chatId, "src/main/resources/images/img_total1.png");
                break;
            default:
                sendMessage(chatId, "Invalid selection. Please choose a valid round.");
                break;
        }
    }

    private void sendRoundImage(Long chatId, String filePath) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        File file = new File(filePath);

        System.out.println("File path: " + filePath);
        System.out.println("File exists: " + file.exists());

        if (file.exists()) {
            sendPhoto.setPhoto(new InputFile(file));
            try {
                execute(sendPhoto);
                System.out.println("Image sent successfully.");
            } catch (TelegramApiException e) {
                System.out.println("Error sending image: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            sendMessage(chatId, "Image file not found.");
        }
    }

    private void sendWelcomeMessage(Long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setText("Assalomu aleykum, quyidagi buyruqlardan birini tanlang: \n⬇️");

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendUnknownCommandMessage(Long userId) {
        sendMessage(userId, "Uzr, ayni vaqtda siz yuborgan xabarni ko'rib chiqa olmaymiz!!!");
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleSavollarBazasi(Update update) {
        Long chatId;
        String callbackData = null;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Savollar bazasi turini tanlang:");

            InlineKeyboardButton officialButton = new InlineKeyboardButton();
            officialButton.setText("Rasmiy tur savollar");
            officialButton.setCallbackData("official_questions");

            InlineKeyboardButton personalButton = new InlineKeyboardButton();
            personalButton.setText("Shaxsiy o'yin savollari");
            personalButton.setCallbackData("personal_game_questions");

            List<List<InlineKeyboardButton>> keyboard = Arrays.asList(
                    Arrays.asList(officialButton, personalButton)
            );

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            keyboardMarkup.setKeyboard(keyboard);
            message.setReplyMarkup(keyboardMarkup);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasCallbackQuery()) {
            // When user selects a callback option
            chatId = update.getCallbackQuery().getMessage().getChatId();
            callbackData = update.getCallbackQuery().getData();

            if ("official_questions".equals(callbackData) || "personal_game_questions".equals(callbackData)) {
                String questionType = callbackData.equals("official_questions") ? "official" : "personal";
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Tanlang raund:");

                // Create round buttons
                InlineKeyboardButton round1Button = new InlineKeyboardButton();
                round1Button.setText("1-tur");
                round1Button.setCallbackData("round_" + questionType + "_1");

                InlineKeyboardButton round2Button = new InlineKeyboardButton();
                round2Button.setText("2-tur");
                round2Button.setCallbackData("round_" + questionType + "_2");

                InlineKeyboardButton round3Button = new InlineKeyboardButton();
                round3Button.setText("3-tur");
                round3Button.setCallbackData("round_" + questionType + "_3");

                List<List<InlineKeyboardButton>> roundKeyboard = Arrays.asList(
                        Arrays.asList(round1Button, round2Button, round3Button)
                );

                InlineKeyboardMarkup roundKeyboardMarkup = new InlineKeyboardMarkup();
                roundKeyboardMarkup.setKeyboard(roundKeyboard);
                message.setReplyMarkup(roundKeyboardMarkup);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackData.startsWith("round_")) {
                String[] parts = callbackData.split("_");
                String questionType = parts[1];
                String roundNumber = parts[2];

                String filePath = "src/main/resources/files";
                if ("official".equals(questionType)) {
                    filePath += "official_round_" + roundNumber + ".pdf";
                } else if ("personal".equals(questionType)) {
                    filePath += "personal_round_" + roundNumber + ".pdf";
                }

                File file = new File(filePath);
                if (file.exists()) {
                    SendDocument sendDocument = new SendDocument();
                    sendDocument.setChatId(chatId);
                    sendDocument.setDocument(new InputFile(file));

                    try {
                        execute(sendDocument);
                    } catch (TelegramApiException e) {
                        System.out.println("Error sending file: " + e.getMessage());
                    }
                } else {
                    SendMessage errorMessage = new SendMessage();
                    errorMessage.setChatId(chatId);
                    errorMessage.setText("Fayl topilmadi: " + file.getName());
                    try {
                        execute(errorMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
