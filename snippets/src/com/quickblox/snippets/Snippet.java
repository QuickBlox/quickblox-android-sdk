package com.quickblox.snippets;

/**
 * User: Oleg Soroka
 * Date: 02.10.12
 * Time: 10:55
 */
public abstract class Snippet {

    String title;
    String subtitle;

    public Snippet(String title) {
        this.title = title;
    }

    protected Snippet(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public abstract void execute();

    public final void performExecution() {
        String stitle = subtitle != null ? String.format(" (%s)", subtitle) : "";
        System.out.println(String.format("Start snippet: %s%s", title, stitle));
        execute();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}