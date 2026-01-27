package com.mad.shared.gpx;

public class CourseDistance {
    private double course = 0.0;
    private double distance = 0.0f;

    public CourseDistance(double course, double distance) {
        if (distance < 0.0) {
            distance = -distance;
            course += 180;
        }
        this.setCourse(course);
        this.distance = distance;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance) {
        if (distance < 0.0) {
            return;
        }
        this.distance = distance;
    }

    public double getCourse() {
        return course;
    }

    public void setCourse(double course) {
//        range: -180 < course <= 180
//                -90 = west, 0 = north, 90 = east, 180 = south
//        if (course <= -180.0f || course > 180.f) {
//            return;
//        }
        this.course = course % 360;
        if (this.course > 180.0) {
            this.course -= 360;
        }
    }
}
