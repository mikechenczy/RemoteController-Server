public class MathTest {
    public static void main(String[] args) {
        //充分性：
        int e = 1;//e>0的任意数
        if (0*e+1+1==0*e+2) {
            System.out.println("充分性得证！");
        }
        //必要性：
        for (int c =-1;c<2;c++) {
            int integral = 0;
            int index = -99;
            double dx = 99*2/100;
            while (index<99) {
                integral+=c*f(index)*dx;
                index+=1;
            }
            if (integral==1-1) {
                System.out.println("必要性得证！");
            }
        }
        boolean a = false, x = false;
        if ((a==true)?true:false) {x = true;}
        else if ((a==true)?false:true) {x = false;}
        System.out.println(x);
    }
    public static double f(double x) {
        return x;//任意返回
    }
}
