package com.cilys.utils.utils.job;

import android.util.Log;

import com.cilys.utils.job.WorkJobRunnable;
import com.cilys.utils.job.impl.WorkResultImpl;
import com.cilys.utils.job.impl.WorkResultListener;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.WhereBuilder;

import java.util.ArrayList;
import java.util.List;

public class CreatePwdRunnable extends WorkJobRunnable {
    public CreatePwdRunnable(WorkResultListener resultListener) {
        super("CreatePwdRunnable", resultListener);
    }

    final long MAX = 999999999;
//    final long MAX = 99;
    final long BETCH = 10000;

    @Override
    public void work() {
        long startTime = System.currentTimeMillis();
        long st = startTime;

        long lastPwd = lastPwd();
        log("TAG", "lastPwd = " + lastPwd);
        long startIndex = lastPwd + 1;

        List<PwdBean> ls = new ArrayList<>();
        for (long i = startIndex; lastPwd <= MAX; i++) {
            String pwd = "17" + fomcat(i);
            ls.add(new PwdBean(pwd));
            if (i % BETCH == 0) {
                DbUtils.getLiteOrm().save(ls);

                log("TAG", "生成密码" + BETCH + "条，当前密码：" + pwd + "，耗时：" + (System.currentTimeMillis() - st) + "毫秒");
                st = System.currentTimeMillis();

                saveLastPwd(pwd);

                try {
                    inProgress(Long.valueOf(pwd), MAX);
                }catch (Exception e){

                }

                ls.clear();
            } else {
                if (MAX - i < BETCH) {
                    DbUtils.getLiteOrm().save(ls);
                    log("TAG", "生成密码" + BETCH + "条，当前密码：" + pwd + "，耗时：" + (System.currentTimeMillis() - st) + "毫秒");

                    saveLastPwd(pwd);

                    try {
                        inProgress(Long.valueOf(pwd), MAX);
                    }catch (Exception e){

                    }

                    ls.clear();
                }
            }
        }
        log("TAG", "生成密码完成，耗时：" + (System.currentTimeMillis() - startTime) + "毫秒");
    }

    private void log(String tag, String msg) {
        System.out.println(msg);
    }

    private long lastPwd(){
        LastPwdBean b = DbUtils.getLiteOrm().queryById("t_pwds_phone_17", LastPwdBean.class);
        if (b == null) {
            return 0;
        }
       try {
           return Long.valueOf(b.getPwd().replace("17", ""));
       }catch (Exception e){

       }

        return 0;
    }

    private void saveLastPwd(String pwd){
        LastPwdBean bean = new LastPwdBean();
        bean.setId("t_pwds_phone_17");
        bean.setPwd(pwd);
        DbUtils.getLiteOrm().save(bean);
    }

    private String fomcat(long i) {
        if (i <=9 ){
            return "00000000" + i;
        }
        if (i <= 99) {
            return "0000000" + i;
        }
        if (i <= 999) {
            return "000000" + i;
        }
        if (i <= 9999) {
            return "00000" + i;
        }
        if (i <= 99999) {
            return "0000" + i;
        }
        if (i <= 999999) {
            return "000" + i;
        }
        if (i <= 9999999) {
            return "00" + i;
        }
        if (i <= 99999999) {
            return "0" + i;
        }
        return String.valueOf(i);
    }
}
