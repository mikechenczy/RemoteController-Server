package com.mj.lrp;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.mj.lrp.controller.CoreController;
import com.mj.lrp.controller.PayController;
import com.mj.lrp.controller.UserController;
import com.mj.lrp.model.Device;
import com.mj.lrp.model.Pay;
import com.mj.lrp.util.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.TimeZone;

/**
 * @author Mike_Chen
 * @date 2023/6/24
 * @apiNote
 */
@SpringBootApplication
public class Application {
    public static boolean alipayDev = false;
    private static Config getOptions() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = alipayDev?"openapi-sandbox.dl.alipaydev.com":"openapi.alipay.com";
        config.signType = "RSA2";

        config.appId = alipayDev?"9021000123628453":"2021002176660183";

        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        config.merchantPrivateKey = alipayDev?"MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCKXmyF4/Tb+8PkS3ADjiC+NTMI36CIPtvM0LKQIU/EowZ+3Tfzaglh74G1+lflr5jldKo+n04MKPbzX+f8mmlbVUqMZQimCm5yX1K24GrQdruq1UkAWYjiyZWm7MU4+UzYW/YFBW5IhgTauKnL6db1CyEOI5d8Clg5dg/qJIwhxvhGq0/bNKw3GM9cuxDhZsECo+taHtCa+hwVeeU5eBq7FAOhMFWZvdpB2XrqnND0v+yiXpmz3rAl3XJa8dsl19hJbN7cWf1cBscwbKo/tll2DZRr9gvBnEdd1rr3SuVgd2p5TagkH5KBorty2YEy8C38KoUSooinSs94+MfMQ2QZAgMBAAECggEBAIJvyLXe0nCdwWhwhMOVM+CaneV7igVl3CYThoDAJkYjuHfXamsYRogAE7LMsBdhlSyY8fPekOB9rW53tmU12qOyn3gBtklwA/XlTj//BjJF+1trfEnMHXJMvbqB8Nloibxb1GufVvse+2yEL+x+H5kYSKzfeGoCBnEEjrQWh9M+K32FaaQwB4RRE5yiFn4OwdcPk+aEdv2mRUWT/jYAawqZG0SSHKf0m9PJJMf/9sbiLdYfsb0Syw8GIUsEPRTmO+t7HnR4VUwDI/E8MLAtdyL/7aBY0WV/tge+Qe9p/U07M6BccM+wE/IK1fUdJlir7Sjow5tzHZ12LqAbQxKGQNECgYEA2Ew5rfbubDMIclgLuvIF95U6qeZCsAJyyedFvnbwXOH/DENvJtwaJm0MlgFDL/ztsJdwMziwlyjCOiqI/K+sIAYXlDp2ErI6ynxfbuDSrD5Ufd9gQQHPxyZ445M+8Q3zdrNZ4V3qbLMVkzn8SfYKkXbmz0aXRdT83ZEZSveHh5MCgYEAo8RW6sPdDNS9cG/idNjDwZiy88HTrzblns6cW+QRKP4TmXIWG4WYJo+2RFykb8MGrLfI9UVoo8edMuncPyvzdgW+okQMSTBRN3n9NmnJaH77iNDQZM3AFpHhknLdcS9CKakDGnooeCiw5Ie/e4kh2o6fEbAJodweIDkmES+XmSMCgYA9KokhJfRUirX3x+hBTJHdasj1JNV0+qxFZm51pyLf0VUTvbogDhwjA7GMdfzvBCKfTqP/CCgABxmt4ztr4a3WPILMex/Kt7Yibrt6Gn/ZD6NLGTEaEQ4V7k4/ZPNmraBMz/k53xoc9SPDx6ENtVNVwX8R3I+IwutjKNYzOf6MmwKBgAIGKTTZmt/PuU+sclYTX8gCxSNyH9Wojgn6b8BK/1NMYJ2i5VcNvaWsXQpXQskG1gxIcTlm1DZB/1Vjwo8dUfMBeyuyP5yf9kEDRvfnMtAR0bYNLqYwTNF6mBkhRk9h0CWHqzwmil2B0z3f16QFO9/hp2GmlVfkypKHYksQFYWVAoGBAKphg5pPzdVn+8L8qcMTsivxVsNdDZE4A/oIEjQmFcvt1dgsbD3xYPwz+9Jq9K5yqTEVH4WA/80xb6UqYZbhTKIV7ATTwlOszM3SsjZp444Fiho/1UUt5zHX1tPUCKb/uhe8jDNUV7AVNnG/DArdZ0vL98YAntif8yK7uDu7A+Xe":
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCyVM6Avu+I0y2HKRb9hRKvmeZVOYnDqKbb9Z6iL9SpItR4NEE42C9mePyPXPW+Dfm6+VaWOEVXNW4DQstYb2WiYkcLvChHWbfkpueFxCDlk/YKQI8T21kUBEJIqH2F1DmtVYNDTGpDlvD1XVqghgE2P47AwRdiTk0vIwSH6BnObzGX5F2cgvcRBdIFNV2Y8hZpsH/7OLpolOtD/PujvxA6IsoY/viRvQZCNuzA4xwCnn3Bk8NtYlkPdMSDc5niNOEZjMkM3zuNYSIHKGaG8AB4MsT9jUqaD+JKCKMsjv3m3KoYBAKZNAwIHIhDqC1Z7C8j7ATI+TmTdgdpImfd9ZV7AgMBAAECggEAEHLsHUn/VDKOZ4ItwbKtl47thPzqrSZ4RZ+xQyqARh2VVYUUYwbl0GDmgt7DOAWX/FwVVYPZOYMPI7Pch8q2qDTCct2nJXWsFaYjgt4lXqe4MRflWqKH4+XEq2kHMvnxXACgO+vnfVaF4Uv8GQRjRjq0kyj8XC/2xJtJRrPs9XJ6KQ5f2REYrHlBieuvvj1wNnTt/u29FYpm/minGttem61T2iKYHS3xv6SYAVLCE/I+vWQEYXgMYpy3qg/RNNuZ1NdXNff2VVdvM0/8ojfFBwsrAarnJjwciOKcRZAI8HhxPJNE9cZuhbIBxyh39zVvVo59brkkQb+S9SKCY3VgsQKBgQD/O5W6bIqdkISWg82kChrvoPXB4MYosgkqwKJvlm45YnLdOLcgBVgYqfVH55N2uxWEZYlMRprKH6NwjxEfzKuPdvkFIPcUvV+pyE/QHXpJpd03mgJ3a5/8s0SBjNRl1X5Tfaun8G4ZqYwsAyd1vEy9LUovwIRZOdZFo2G72P2skwKBgQCy3grBiHCMFb7vOjT2aqqqc6Tp5fIqlNDCIV966+Hdcm7wf58IM1/DuqEdFNJRWyvcnrVlWCPthD8rb5manzioOFvHFafaXBBahvVjHlud4sy4bCSaIaL6w3sHvcih9MRI7GVq1bBg5SB0QcNI4vk+jVE+DLWhACXN2rIU61tseQKBgHWq/L5zp/w4Ukgp/7ZuyjhLlsGJZRIB8mP5Z2nWin5+OsJukU576D+Gq4Q46S1F1wew3/mAW4Bv0aPk8Vodu8JqcSfCKOcw31eg99rUnDoz86bco+J2hc20wBKR4KIKKTFIsMi9+aRvHCZ2VczbwO0+YgNd2CZimbyFNFusEFdtAoGBAJjuZD/oymHbze541APEW7t1ORGLO6zQpFT3d81/lRRj3Rrf4zd9xWlyToKw5MoOq8fsIqhN3hocMm6O8sRTUnvyA/aKO4Plp03fijt/H1I1MliUjg8Cp3jEXpLV348p2hGHnYbkwfN2tHLyL2hIOFcVONtWvoyztZ72V8CvIvHxAoGAWoewgKJztMJQ7hAlJQ0f8wOG55np1qGa+Q3sZRfGrI76O/G32P3y3HMe7tzQ+LAiuZz5QL0gvzxtMsPxfZIpkgINrBgtU6ovDKBB4TxcJ0m0eOshF47SRoXWqLRsWnGFKNPKRACM2lkpC4kTmdFva72129JOW7ZAyXCIWC2YTi4=";
        //注：证书文件路径支持设置为文件系统中的路径或CLASS_PATH中的路径，优先从文件系统中加载，加载失败后会继续尝试从CLASS_PATH中加载
        //config.merchantCertPath = "<-- 请填写您的应用公钥证书文件路径，例如：/foo/appCertPublicKey_2019051064521003.crt -->";
        //config.alipayCertPath = "<-- 请填写您的支付宝公钥证书文件路径，例如：/foo/alipayCertPublicKey_RSA2.crt -->";
        //config.alipayRootCertPath = "<-- 请填写您的支付宝根证书文件路径，例如：/foo/alipayRootCert.crt -->";

        //注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
        config.alipayPublicKey = alipayDev?"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsHSw683xhY+PvtsW3Phaedhm9gacKLHyg+9B+4s6wmWSd21PTisMVHBRZdhIPLC38ws2R+OTcLVcAYnLka3b25Uje3tNleAzfMjgEvGaWaoipg3b1vMgUsKQH8q0OPU5c2Zu34SXRTnklT5Jzuid7LyPPkEO8FM+ShldIq326h8PimW5iRf0czfviOP1qFhLg4rSlyPuEEenLz+ns/lekfpGYDEvPOYaDHIANKl+rcLWR5szOtVuduP2U1wtcWQnoD0vuZz/KVW86cKMNuL+LLBlXLQtPKhzgDD563KrwcEAOT/Yvy0wvdDcnyyERzObBDBOuO2ed0lmc4Ybp0jXMwIDAQAB":
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4G1UzQdGU8XwfC+UA+1MJcX/0xJzfJFby3TuMepfjYa5Z/XeXTL2hFk9UZSE8ZgkngtjhVvPPh8Q+GxeV1gAwFwWfUkxOGdpC9GGnkgD4b0Ij/K9v8XSRTPO+YoZC6mqtin1PIC5ut4GZ2XsSxWGVsa4uhUw2PTePt9knfws7vAmfpUuD7Aw3sHQbM7A0ag+bFHlBkS1+lb7nbs2V5N90OFCRms/0eQjt7yEZ5Cjy8b+yUzyZS6cjsO8AwJmXyhwICfoLzxmp54sfZlqU3wWAiJHoHBtC+++/LRXNrmnH536vGqw9wujIFHc8/m7XH/4qiFDJW0UKJxItl6K7MAfGQIDAQAB";
        //可设置异步通知接收服务地址（可选）
        //config.notifyUrl = "<-- 请填写您的支付类接口异步通知接收服务地址，例如：https://www.test.com/callback -->";

        return config;
    }

    public static void main(String[] args) {
        System.setProperty("javax.xml.accessExternalDTD", "all");
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(Application.class, args);
        Factory.setOptions(getOptions());
        synchronized (PayController.payController.payService) {
            List<Pay> payList = PayController.payController.payService.getAll();
            for(Pay pay : payList) {
                if (System.currentTimeMillis() - pay.getCreateTime() >= Long.parseLong(CoreController.coreController.infoService.getByDescription("支付过期时间").getContent())) {
                    try {
                        Factory.Payment.Common().close(pay.getPayNo());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //com.alipay.demo.trade.Pay.closeOrder(pay.getPayNo());
                    PayController.payController.payService.delete(pay.getPayNo());
                }
                new Thread(() -> {
                    try {
                        Thread.sleep(Long.parseLong(CoreController.coreController.infoService.getByDescription("支付过期时间").getContent()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (PayController.payController.payService) {
                        if (PayController.payController.payService.getByPayNo(pay.getPayNo()) != null) {
                            try {
                                Factory.Payment.Common().close(pay.getPayNo());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //com.alipay.demo.trade.Pay.closeOrder(pay.getPayNo());
                            PayController.payController.payService.delete(pay.getPayNo());
                        }
                    }
                }).start();
            }
        }
        //new Server(Define.port).start();
        new Thread(() -> {
            for(Device device : UserController.userController.deviceService.getAll()) {
                device.setControlId(0);
                UserController.userController.deviceService.update(device);
            }
            try {
                Thread.sleep(Utils.getTomorrowZeroMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (CoreController.coreController.signInService) {
                CoreController.coreController.signInService.deleteAll();
            }
            synchronized (CoreController.coreController.loginInfoService) {
                CoreController.coreController.loginInfoService.deleteAll();
            }
            while (true) {
                try {
                    Thread.sleep(1000);//Make sure tomorrow zero millis correct.
                    Thread.sleep(Utils.getTomorrowZeroMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (CoreController.coreController.signInService) {
                    CoreController.coreController.signInService.deleteAll();
                }
                synchronized (CoreController.coreController.loginInfoService) {
                    CoreController.coreController.loginInfoService.deleteAll();
                }
            }
        }).start();
    }
}
