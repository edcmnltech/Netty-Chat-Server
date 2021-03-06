package com.test.chatserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameDecoder;

/**
 *  Decodes ByteBufs by blocking until they are ready to be passed down
 *  the channel pipeline. If the handler receives HttpContent, casts the content
 *  to ByteBuf and sends it upstream.
 *  
 *  Each segment of data should be prefixed by a 4 byte integer indicating
 *  its length.
 * 
 * @author jalbatross (Joey Albano)
 *
 */

public class ChatServerIntFrameDecoder extends SimpleChannelInboundHandler<Object> {
    
    public static final int MAX_BYTES = 1024;
    private Stack<Object> out = new Stack<Object>();
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ChatServerDecoder] Sending upstream!");

        if (msg instanceof HttpContent) {
            System.out.println("[ChatServerDecoder] Got http content");
            HttpContent httpCred = (HttpContent) msg;
            
            ctx.fireChannelRead(httpCred.content());
            
            
            return;
        }
        else if (msg instanceof ByteBuf){
            System.out.println("[ChatServerDecoder] Got ByteBuf");
            ByteBuf buf = (ByteBuf) msg;
            
            IntegerHeaderFrameDecoder decoder = new IntegerHeaderFrameDecoder();
            
            decoder.decode(ctx, buf, out);
           
            ctx.fireChannelRead(out.pop());
        }
        else {
            System.out.println("[ChatServerDecoder] Got message of type: " 
                                 +  msg.getClass()+ " . DISCARDING");
        }
        
        return;
        
    }
    

}
