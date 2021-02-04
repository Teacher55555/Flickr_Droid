package com.armageddon.android.flickrdroid.model;


public class PhotoInfo {
    private Comments comments;
    private String favsQuantity;
    private static class Title {private String _content;}
    private static class Description {private String _content;}
    private static class Visibility {
        private int ispublic;
        private int isfriend;
        private int isfamily;

        public int getIspublic() {
            return ispublic;
        }

        public int getIsfriend() {
            return isfriend;
        }

        public int getIsfamily() {
            return isfamily;
        }
    }
    private static class Dates {
        private String posted;
        private String taken;

        public String getPosted() {
            return posted;
        }

        public String getTaken() {
            return taken;
        }
    }

    private static class Views {private String _content;}
    private static class Comments {private String _content;}
    public static class Tag {
        String raw;
        String _content;

        public String get_content() {
            return _content;
        }

        public String getRaw() {
            return raw;
        }
    }

    public String getCommentsQuantity() {
        return comments._content;
    }

    public void setFavsQuantity(String favsQuantity) {
        this.favsQuantity = favsQuantity;
    }

    public String getFavsQuantity() {
        return favsQuantity;
    }
}
