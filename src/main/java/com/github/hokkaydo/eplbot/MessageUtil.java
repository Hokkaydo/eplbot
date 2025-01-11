package com.github.hokkaydo.eplbot;

import com.github.hokkaydo.eplbot.configuration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Convenience class to handle various message-related operations
 * */
public class MessageUtil {

    private static final String HASTEBIN_API_POST_URL = "https://hastebin.com/documents/";
    private static final String HASTEBIN_SHARE_BASE_URL = "https://hastebin.com/share/%s";
    public static final int HASTEBIN_MAX_CONTENT_LENGTH = 350_000;

    private static HttpRequest.Builder hastebinPostRequest = null;

    /**
     * Convert a {@link Message} to an embed with original attachments and apply the modification on already existing
     * {@link EmbedBuilder} then send the message and process the message using the given {@link Consumer}
     * @param message the message to convert
     * @param send a {@link Function} mapping an {@link EmbedBuilder} to a {@link MessageCreateAction}
     * @param processSentMessage a {@link Consumer} to process the message after it has been sent
     * */
    public static void toEmbedWithAttachements(Message message, Function<EmbedBuilder, MessageCreateAction> send, Consumer<Message> processSentMessage) {
        MessageCreateAction action = send.apply(toEmbed(message)).addFiles();
        message.getAttachments().stream()
                .map(m -> new Tuple3<>(m.getFileName(), m.getProxy().download(), m.isSpoiler()))
                .map(MessageUtil::mapToFileUpload)
                .map(c -> c != null ? c.thenAccept(action::addFiles) : CompletableFuture.completedFuture(null))
                .reduce((a,b) -> {a.join(); return b;})
                .ifPresentOrElse(
                        c -> {
                            c.join();
                            action.queue(processSentMessage);
                        },
                        () -> action.queue(processSentMessage)
                );
    }

    /**
     * Convert a {@link Message} to an {@link EmbedBuilder}
     * @param message the message to convert
     * @return the {@link EmbedBuilder} representing the message
     * */
    public static EmbedBuilder toEmbed(Message message) {
        return new EmbedBuilder()
                       .setAuthor(message.getAuthor().getName(), message.getJumpUrl(), message.getAuthor().getAvatarUrl())
                       .appendDescription(message.getContentRaw())
                       .setTimestamp(message.getTimeCreated())
                       .setFooter(message.getGuild().getName() + " - #" + message.getChannel().getName(), message.getGuild().getIconUrl());
    }

    /**
     * Utility method to map a {@link Tuple3} (filename, file stream, is a spoiler)
     * to a {@link CompletableFuture} of {@link FileUpload}
     * @param tuple3 the tuple to map
     * @return a {@link CompletableFuture} of {@link FileUpload} or null if the file is too big
     * */
    private static CompletableFuture<FileUpload> mapToFileUpload(Tuple3<String, CompletableFuture<InputStream>, Boolean> tuple3) {
        return tuple3.b()
                       .thenApply(MessageUtil::readInputStream)
                       .thenApply(bytes -> bytes.length > 0 ? FileUpload.fromData(bytes, tuple3.a()) : null)
                       .thenApply(upload -> {
                           if(upload == null) return null;
                           if (Boolean.TRUE.equals(tuple3.c())) return upload.asSpoiler();
                           return upload;
                       });
    }

    /**
     * Read an {@link InputStream} and return its content as a byte array
     * @param i the {@link InputStream} to read
     * @return the content of the file's {@link InputStream} as a byte array or an empty array if the file is too big
     * */
    private static byte[] readInputStream(InputStream i) {
        try {
            byte[] bytes = i.readAllBytes();
            int length = bytes.length;
            if (length > Main.getJDA().getSelfUser().getAllowedFileSize())
                return new byte[0];
            return bytes;
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private MessageUtil() {}

    /**
     * Send a message to the admin channel of the guild
     * @param message the message to send
     * @param guildId the id of the guild
     * */
    public static void sendAdminMessage(String message, Long guildId) {
        String adminChannelId = Config.getGuildVariable(guildId, "ADMIN_CHANNEL_ID");
        TextChannel adminChannel;
        if(adminChannelId.isBlank() || (adminChannel = Main.getJDA().getChannelById(TextChannel.class, adminChannelId)) == null) {
            Main.LOGGER.warn("Invalid admin channel : {}", Objects.requireNonNull(Main.getJDA().getGuildById(guildId)).getName());
            return;
        }
        adminChannel.sendMessage(message).queue();
    }

    /**
     * Get the name and nickname of a user
     * @param member the {@link Member} to get the nickname from
     * @param user the {@link User} to get the name from
     * @return the name and nickname of the user
     * */
    public static String nameAndNickname(@Nullable Member member, User user) {
        boolean hasNickname = member != null && member.getNickname() != null;
        return (hasNickname  ? member.getNickname() + " (" : "") + user.getEffectiveName() + (hasNickname ? ")" : "");
    }

    /**
     * Post content on Hastebin and return a shareable link
     * @param client an {@link HttpClient} initialized by the caller to make multiple calls on the same client
     * @param data the content to post
     * @return a {@link CompletableFuture} returning the shareable link or an empty string if an error arises
     * */
    public static CompletableFuture<String> hastebinPost(HttpClient client, String data) {
        if(data.length() > HASTEBIN_MAX_CONTENT_LENGTH) throw new IllegalArgumentException("'data' should be shorted than %d".formatted(HASTEBIN_MAX_CONTENT_LENGTH));
        if (hastebinPostRequest == null)
            hastebinPostRequest = HttpRequest.newBuilder()
                                          .header("Content-Type", "text/plain")
                                          .header("Authorization", "Bearer %s".formatted(System.getenv("HASTEBIN_API_TOKEN")))
                                          .uri(URI.create(HASTEBIN_API_POST_URL));

        return client.sendAsync(hastebinPostRequest.POST(HttpRequest.BodyPublishers.ofString(data)).build(), HttpResponse.BodyHandlers.ofString())
                       .thenApply(HttpResponse::body)
                       .thenApply(JSONObject::new)
                       .thenApply(response -> {
                           if (!response.has("key")) {
                               return "";
                           }
                           return HASTEBIN_SHARE_BASE_URL.formatted(response.get("key"));
                       });
    }

    private record Tuple3<A, B, C>(A a, B b, C c) {}


}