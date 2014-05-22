/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.client;

import java.util.GregorianCalendar;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Pirmin
 * 
 * DocEntry and not Chatlog, because I want to use this class in a other module too
 */
@XmlRootElement(name = "DocEntry")
public class DocEntry {
    
    public enum DOCENTRYTYPE{
        CHATLOG,
        INFO,
        STORY,
        TASK
    }
    
    @XmlElement(name =  "type")
    private DOCENTRYTYPE type;
    
    @XmlElement(name = "date")
    private GregorianCalendar date = new GregorianCalendar();
    
    @XmlElement(name = "content")
    private String content;
    
    @XmlElement(name = "name")
    private String name;

    @XmlTransient
    public DOCENTRYTYPE getType() {
        return type;
    }

    public DocEntry() {
    }

    public DocEntry(DOCENTRYTYPE type, String content, String name) {
        this.type = type;
        this.content = content;
        this.name = name;
    }   

    public void setType(DOCENTRYTYPE type) {
        this.type = type;
    }

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    @XmlTransient
    public GregorianCalendar getDate() {
        return date;
    }    

    @XmlTransient
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @XmlTransient
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
