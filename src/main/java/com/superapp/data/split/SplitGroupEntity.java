package com.superapp.data.split;

import com.superapp.data.GroupEntity;
import com.superapp.data.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class SplitGroupEntity {

    private String groupId;
    private String superapp;
    private UserEntity groupLeader;
    private List<UserEntity> allUsers;
    private String avatar;
    private ArrayList<SplitTransaction> expenses;
    private String splitTitle;

    public SplitGroupEntity() {
    }


    public SplitGroupEntity(GroupEntity group, String splitTitle) {
        this.groupId = group.getGroupId();
        this.superapp = group.getSuperapp();
        this.groupLeader = group.getGroupLeader();
        this.avatar = group.getAvatar();
        this.splitTitle = splitTitle;
        this.expenses = new ArrayList<SplitTransaction>();
        this.allUsers = group.getAllUsers();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public ArrayList<SplitTransaction> getExpenses() {
        return expenses;
    }

    public void setExpenses(ArrayList<SplitTransaction> balances) {
        this.expenses = balances;
    }

    public String getSplitTitle() {
        return splitTitle;
    }

    public void setSplitTitle(String splitTitle) {
        this.splitTitle = splitTitle;
    }


    public void addNewTransaction(SplitTransaction newTran) {
        this.expenses.add(newTran);
    }

    public String getSuperapp() {
        return superapp;
    }

    public void setSuperapp(String superapp) {
        this.superapp = superapp;
    }

    public UserEntity getGroupLeader() {
        return groupLeader;
    }

    public void setGroupLeader(UserEntity groupLeader) {
        this.groupLeader = groupLeader;
    }

    public List<UserEntity> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<UserEntity> allUsers) {
        this.allUsers = allUsers;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public GroupEntity getGroup(){//    public GroupEntity(String groupId, UserEntity groupLeader, List<UserEntity> allUsers, String avatar) {
        return new GroupEntity(this.groupId,this.groupLeader,this.allUsers,this.avatar);
    }
}
