package com.jfocht.AudioRecorderApp;

import java.util.Date;


public class AudioClip {
    private final Integer id;
    private final String name;
    private final String filename;
    private final Integer duration;
    private final Date created;

    public AudioClip(String name, String filename, Integer duration) {
        this.id = null;
        this.name = name;
        this.filename = filename;
        this.duration = duration;
        this.created = null;
    }

    public AudioClip(Integer id, String name, String filename, Integer duration, Date created) {
        this.id = id;
        this.name = name;
        this.filename = filename;
        this.duration = duration;
        this.created = created;
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getFilename() {
        return this.filename;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public Date getCreated() {
        return this.created;
    }

}
