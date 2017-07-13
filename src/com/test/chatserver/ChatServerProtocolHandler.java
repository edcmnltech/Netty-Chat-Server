package com.test.chatserver;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecoder;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

/**
 * Identifies the protocol that clients are using to send credentials with, 
 * then delegates the credentials to the appropriate handler based on the 
 * received format.
 * 
 * Current valid credential formats are JSON using HTTP POST conforming to the
 * following schema: 
 * 
 * {username: aUsername, password: aPassword}
 * 
 * and Flatbuffers serialized credentials as specified in the Schema package
 * of this project.
 * 
 * @author Jalbatross (Joey Albano)
 *
 */

public class ChatServerProtocolHandler extends HttpRequestDecoder {
    private ByteBuf buf;
    private List<Object> decoded = new Stack<Object>();
    
    //number of characters to check at end of HTML request to find JSON 
    //message
    public static final int JSON_BUFFER_SIZE = 128;
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ChatServerProtocolHandler] channelread called");

        buf = (ByteBuf) msg;
        if (buf.readableBytes() < 5) {
            return;
        }
        
        final int magic1 = buf.getUnsignedByte(buf.readerIndex());
        final int magic2 = buf.getUnsignedByte(buf.readerIndex() + 1);
        final int readableBytes = buf.readableBytes();
        
        //If POST, forward the message to the HTTP post decoder
        if (isPost(magic1,magic2)){
            ctx.channel().pipeline().replace(this, "httpServerCodec", new HttpServerCodec());
            
            
            
        /*
            //Find the JSON object, starting from the end of the HTTP message
            char[] chars = new char[JSON_BUFFER_SIZE];
            int val = 0;
            for (int i = 0; i < chars.length; i++) {
                val = buf.getUnsignedByte(buf.readableBytes() - chars.length + i);
                chars[i] = (char) val;
            }
            
            boolean jsonComplete = false;
            int startJson, endJson, currentIndex;
            
            startJson = endJson = 0;
            currentIndex = chars.length - 1;
            
            //Find the JSON object
            while (!jsonComplete && currentIndex > 0) {
                
                if (chars[currentIndex] == '}') {
                    endJson = currentIndex;
                }
                if (chars[currentIndex] == '{') {
                    startJson = currentIndex;
                    jsonComplete = true;
                }
                currentIndex--;
            }
            
            //make sure it's valid
            if (startJson > endJson || 
                endJson - startJson > JSON_BUFFER_SIZE) {
                ctx.close();
                return;
            }
            
            String credJson = new String();
            
            for (int i = 0; i < endJson - startJson + 1; i++) {
                credJson += chars[startJson + i];
            }
            
            JsonObject obj = new JsonParser().parse(credJson).getAsJsonObject();
            System.out.println(obj.get("name"));
            System.out.println(obj.get("password"));
            
            String theName = obj.get("name").getAsString();
            System.out.println(theName);
            
            return;*/
        }
        //Otherwise forward the msg to the integer based frame decoder
        else {
            ctx.channel().pipeline().replace(this, "intFrameDecoder", new ChatServerIntFrameDecoder());
        }

        ctx.fireChannelRead(msg);
    }

    private static boolean isPost(int magic1, int magic2) {
        return  magic1 == 'P' && magic2 == 'O';
    }





}