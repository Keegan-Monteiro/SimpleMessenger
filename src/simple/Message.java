package simple;

import java.io.Serializable;

public class Message implements Serializable{
    
    private static final long serialVersionUID = 12L;
    public String type, sender, content, time;
    
    public Message(String type, String sender, String content, String time){
        this.type = type; this.sender = sender; this.content = content; this.time = time;
    }
    
    @Override
    public String toString(){
        return "{type='"+type+"', sender='"+sender+"', content='"+content+"', time='"+time+"'}";
    }
}
