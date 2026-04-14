package com.hndl.ui.model;

import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

@Table("overload_model")
public class OverloadRecordModel{

    // 指定自增，每个对象需要有一个主键
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    // 非空字段 时间
    @NotNull
    private String start_date;
    private int work_arm;
    private int magnification;
    private int leg;
    private int fifth_leg;
    private int work_area;
    private String l1;
    private String a;
    private String p1;
    private String p2;
    private String amplitude;
    private String frontal_weight;
    private String actual_weight;
    private String moment_percentage;
    private String end_date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWork_arm() {
        return work_arm;
    }

    public void setWork_arm(int work_arm) {
        this.work_arm = work_arm;
    }

    public int getMagnification() {
        return magnification;
    }

    public void setMagnification(int magnification) {
        this.magnification = magnification;
    }

    public int getLeg() {
        return leg;
    }

    public void setLeg(int leg) {
        this.leg = leg;
    }

    public int getFifth_leg() {
        return fifth_leg;
    }

    public void setFifth_leg(int fifth_leg) {
        this.fifth_leg = fifth_leg;
    }

    public int getWork_area() {
        return work_area;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getL1() {
        return l1;
    }

    public void setL1(String l1) {
        this.l1 = l1;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }

    public String getP2() {
        return p2;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }

    public String getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(String amplitude) {
        this.amplitude = amplitude;
    }

    public String getFrontal_weight() {
        return frontal_weight;
    }

    public void setFrontal_weight(String frontal_weight) {
        this.frontal_weight = frontal_weight;
    }

    public String getActual_weight() {
        return actual_weight;
    }

    public void setActual_weight(String actual_weight) {
        this.actual_weight = actual_weight;
    }

    public String getMoment_percentage() {
        return moment_percentage;
    }

    public void setMoment_percentage(String moment_percentage) {
        this.moment_percentage = moment_percentage;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public void setWork_area(int work_area) {
        this.work_area = work_area;
    }


}
