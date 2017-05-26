package bot;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.*;

/**
 * Created by Aluno on 16/09/2016.
 */

public class Bot {
    TelegramBot bot = TelegramBotAdapter.build("225486233:AAGSX_ghHrtXdtlXeyEjyWUKCaTlqb5P4y8");
    Update update;
    InlineQuery inlineQuery;
    Message msg;
    Chat chat;
    Model model;
    Boolean countrySearch = false;
    Boolean nameSearch = false;

    // TODO Wasn't able to include Unicode.

    /*
    //String mGLass = "\u0001f50d";
    char[] mGLass = Character.toChars(128269);
    String man = "\u0001f471";
    String usFlag = "\u0001f1fa\u0001f1f8";
    String star = "\u2b50\ufe0f";
    String pallet = "\u0001f3a8";
    String showRandomArtistMessage = "Show random artist " + pallet;
    String searchArtistByNameMessage = "Search artists by name " + mGLass + " " + man;
    String searchArtistsByCountryMessage = "Search artists by country " + mGLass + " " + usFlag;
    String rateMeMessage = "Rate me " + star;
    */

    String showRandomArtistMessage = "Show random artist";
    String searchArtistByNameMessage = "Search artists by name" ;
    String searchArtistsByCountryMessage = "Search artists by country";
    String rateMeMessage = "Rate me";

    Bot(){
        model = new Model();
    }

    public void sendMessage(String text , String chatId){
        bot.execute(
                new SendMessage(chatId, text)
        );
   }
    public void sendPhoto(String chat, String photoLink, Artist artist) throws IOException  {
        Image image = null;
        URL url = new URL(photoLink);
        image = ImageIO.read(url);
        BufferedImage originalImage = (BufferedImage) image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("More from " + artist.getNome()).url(artist.getLink())
                });

        bot.execute(
                new SendPhoto(chat, imageInByte).caption(artist.mountArtist()).replyMarkup(inlineKeyboard)
        );
    }

    protected void setUpdate(String response){
        update = BotUtils.parseUpdate(response);
    }


    protected void read(byte[] bodyRequest) {
        try {
            String response = new String(bodyRequest, "UTF-8");
            setUpdate(response);

            if (isCommonChat(response)) {
                setCommonChat();
                String message = getMessage();

                if (isStartingMessage(message)){
                    sendStartingMessage();
                    showKeyboard();
                }
                else if (isCountrySearch()){
                    searchArtistsByCountry(message);
                    showKeyboard();
                    setCountrySearch(false);
                }
                else if(isNameSearch()){
                    searchArtistsByName(message);
                    showKeyboard();
                    setNameSearch(false);
                }
                if(isRandomMessage(message)) {
                    showRandomArtist();
                    showKeyboard();
                } else if(isSearchArtistByNameMessage(message)) {
                    sendMessage("Please type an artist name for me to search for", getChatId());
                    setNameSearch(true);
                }
                else if(isSearchArtistByCounty(message)) {
                    sendMessage("Please type an artist country for me to search for", getChatId());
                    setCountrySearch(true);
                }
                else if(isRateMeMessage(message)){
                    sendMessage("Thanks for using my services! Please rate me if you've liked them! ;)",
                                getChatId());

                    // TODO Logic to send the option to rate the bot here.
                }

            } else {
                setInlineQuery();
                String message = inlineQuery.query();
                inlineSearch(message);
            }

        } catch (Exception e) {
            // Admin notification
            sendMessage(e.getMessage() + "\n" + e.getStackTrace(), "136505761"); // Antigo "-145562622"
        }
    }

    private boolean isRateMeMessage(String message) {
        return message.contentEquals(rateMeMessage);
    }

    private boolean isSearchArtistByCounty(String message) {
        return message.contentEquals(searchArtistsByCountryMessage);
    }

    private boolean isSearchArtistByNameMessage(String message) {
        return message.contentEquals(searchArtistByNameMessage);
    }

    private boolean isRandomMessage(String message) {
        return message.contentEquals(showRandomArtistMessage);
    }

    private boolean isStartingMessage(String message) {
        return message.contentEquals("/start");
    }

    private void sendStartingMessage(){
        sendMessage("Hello! I'm glad you're here. My name is ArteiroBot, but you can call me " +
                "Art, Artist or Bob. I can show you artists and artworks from all over the world. " +
                "You can search an artist by name or location, or I can show you a random artist. " +
                "To get started, chose one of the below options from the keyboard.\n\n" +
                "P.S. You can also call me in another chat. Just type @arteirobot and share artists " +
                "with your friends!", getChatId());
    }

    private void searchArtistsByCountry(String country) {
        ArrayList<Artist> artists = getArtistsByCountry(country);
        if (artists.size() != 0){
            for (Artist artist : artists) {
                try {
                    sendPhoto(getChatId(), artist.getArte(), artist);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else{
            sendMessage("No artists found.", getChatId());
        }
    }

    private void showRandomArtist() throws IOException {
        Artist artist = model.showRandomArtist();
        sendPhoto(getChatId(), artist.getArte(), artist);
    }

    private ArrayList<Artist> getArtistsByCountry(String country){
        ArrayList<Artist> artists = model.searchArtistByCountry(country);
        return artists;
    }
    private ArrayList<Artist> getArtistsByName(String name){
        ArrayList<Artist> artists = model.searchArtistName(name);
        return artists;
    }

    private void inlineSearch(String name){

        ArrayList<Artist> artists = getArtistsByName(name);

        if(artists.size() != 0) {
            InlineQueryResult[] result = new InlineQueryResultArticle[artists.size()];
            int i = 0;
            for (Artist artist : artists) {
                result[i++] = new InlineQueryResultArticle(
                        String.valueOf(i),
                        artist.getNome(),
                        artist.getLink()).thumbUrl(artist.getArte()
                ).description(artist.getLocation());
            }

            bot.execute(new AnswerInlineQuery(inlineQuery.id(), result));
        }
    }

    private void searchArtistsByName(String name) {
        ArrayList<Artist> artists = getArtistsByName(name);
        if (artists.size() != 0){
            for (Artist artist : artists) {
                try {
                    sendPhoto(getChatId(), artist.getArte(), artist);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else{
            sendMessage("No artists found.", getChatId());
        }

    }

    private boolean isCommonChat(String response){
        return response.contains("chat");
    }

    private void setInlineQuery(){
        inlineQuery = update.inlineQuery();
    }

    private void setCommonChat() {
        msg = update.message();
        chat = msg.chat();
    }

    private void showKeyboard(){

        ReplyKeyboardMarkup searchArtistsKeyboard = new ReplyKeyboardMarkup(
                new String[]{searchArtistByNameMessage},
                new String[]{searchArtistsByCountryMessage},
                new String[]{showRandomArtistMessage}
                //new String[]{rateMeMessage}

        ).resizeKeyboard(true).oneTimeKeyboard(true);

        bot.execute(
                new SendMessage(getChatId(), "What do you want to do?").replyMarkup(new ForceReply()).replyMarkup(searchArtistsKeyboard)
        );
    }

    protected String getChatId(){
        return Long.toString(chat.id());
    }

    protected String getMessage(){
        return msg.text();
    }

    public void setCountrySearch(boolean isCountrySearch) {
        this.countrySearch = isCountrySearch;
    }

    public void setNameSearch(boolean isNameSearch) {
        this.nameSearch = isNameSearch;
    }

    public boolean isNameSearch() {
        return nameSearch;
    }

    public boolean isCountrySearch() {
        return countrySearch;
    }
}
