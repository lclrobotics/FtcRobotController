package org.firstinspires.ftc.teamcode.mastercode;




import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.mastercode.UltimategoalHardware;
import org.firstinspires.ftc.teamcode.robotutils.MathFunctions;
import org.firstinspires.ftc.teamcode.robotutils.RobotMovement;
import org.firstinspires.ftc.teamcode.robotutils.MiniPID;


    @Autonomous(name="Phoenix Auto PID Tuning", group="PID")
//@Disabled
    public class Comp_Auto_2 extends LinearOpMode {

        public double z_angle;
        public double globalAngle;
        public double deltaAngle;
        UltimategoalHardware robot = new UltimategoalHardware();

        MiniPID controllerAngle = new MiniPID(0.021, 0.333, 0.08); //.025
        MiniPID controllerDrive = new MiniPID(0.00, 0, 0.00); //.025
        //Past working values .035, 0, .03

        //Ziegler-Nichols standard for starting PID tuning value
        //Kcr = Proportional gain that causes steady osscillation (.04)
        //Pcr = Period of Kcr's Oscillation (measured in seconds) (1.4s) T
        //In a full PID system:
        //Proportional: .8Kcr
        //Derivative: (Ku*Tu)/10

        // called when init button is  pressed.

        /**
         * Get current cumulative angle rotation from last reset.
         * @return Angle in degrees. + = left, - = right from zero point.
         */

        public void setAngle(){
            Orientation angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            double deltaAngle = angles.firstAngle;
            globalAngle = deltaAngle;
        }

        public double getAngle() {
            // We experimentally determined the Z axis is the axis we want to use for heading angle.
            // We have to process the angle because the imu works in euler angles so the Z axis is
            // returned as 0 to +180 or 0 to -180 rolling back to -179 or +179 when rotation passes
            // 180 degrees. We detect this transition and track the total cumulative angle of rotation.

            Orientation angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            deltaAngle = angles.firstAngle;

            return -RobotMovement.AngleWrap(deltaAngle - globalAngle);
        }





        public double getDistance(){

                double distance = robot.dSensorFront.getDistance(DistanceUnit.CM);
                return distance;




        }

        public void drivePID(double power, double goalAngle, int direction, double goal) {//-180 to 180
            double starTime = System.currentTimeMillis();
            controllerDrive.setOutputLimits(-1,1);
            while (true) {
                double correction = controllerDrive.getOutput(getAngle(), goalAngle);
                telemetry.addData("Distance",getDistance());
                telemetry.update();
                double y = -direction * power;
                double x = 0;
                double z = correction;
                robot.frontleftDrive.setPower(y + x + z);
                robot.frontrightDrive.setPower(-y + x + z);
                robot.backleftDrive.setPower(y - x + z);
                robot.backrightDrive.setPower(-y - x + z);

               double abserror = Math.abs(getDistance()-goal);
                if(abserror <= robot.tolerancePID2) {
                    stopDrive();
                    break;
                }
                if(isStopRequested() == true){
                    stopDrive();
                    stop();
                    break;
                }
            }
        }

        public void drivePIDtime(double power, double goalAngle, int direction, double time, int sensor) {//-180 to 180
            double starTime = System.currentTimeMillis();
            controllerDrive.setOutputLimits(-1,1);
            while (true) {
                double correction = controllerDrive.getOutput(getAngle(), goalAngle);
                telemetry.addData("Distance",getDistance());
                telemetry.update();
                double y = -direction * power;
                double x = 0;
                double z = correction;
                z = z * robot.turnFactorPID;
                robot.frontleftDrive.setPower(y + x + z);
                robot.frontrightDrive.setPower(-y + x + z);
                robot.backleftDrive.setPower(y - x - z);
                robot.backrightDrive.setPower(-y - x - z);
                if(System.currentTimeMillis()-starTime >= time) {
                    stopDrive();
                    break;
                }
                if(isStopRequested() == true){
                    stopDrive();
                    stop();
                    break;
                }
            }
        }

        public void strafePID(double power, double goalAngle, int direction, double goal) {//-180 to 180
            double starTime = System.currentTimeMillis();
            controllerDrive.setOutputLimits(-1,1);
            while (true) {
                double correction = controllerDrive.getOutput(getAngle(), goalAngle);
                telemetry.addData("Angle:", getAngle()); //Gives our current pos
                telemetry.addData("Hot Garb:", correction);
                telemetry.addData("Global Subtract", globalAngle);
                telemetry.update();
                double y = -direction*power;
                double x = 0;
                double z = correction;
                robot.frontleftDrive.setPower(-y + x + z);
                robot.frontrightDrive.setPower(-y + x + z);
                robot.backleftDrive.setPower(y - x + z);
                robot.backrightDrive.setPower(y - x + z);

                if(isStopRequested() == true){
                    stopDrive();
                    stop();
                    break;
                }

            }
        }

        public void stopDrive(){
            robot.frontleftDrive.setPower(0);
            robot.frontrightDrive.setPower(0);
            robot.backleftDrive.setPower(0);
            robot.backrightDrive.setPower(0);
        }





        public void strafeLeft(double power, long time){
            robot.frontrightDrive.setPower(-power);
            robot.backrightDrive.setPower(power);
            robot.frontleftDrive.setPower(-power);
            robot.backleftDrive.setPower(power);
            sleep(time);
            stopDrive();

        }


        public void strafeRight(double power, long time){
            robot.frontrightDrive.setPower(power);
            robot.backrightDrive.setPower(-power);
            robot.frontleftDrive.setPower(power);
            robot.backleftDrive.setPower(-power);
            sleep(time);
            stopDrive();

        }


        public void turnToAnglePID(double goalAngle){//-180 to 180
            controllerAngle.setOutputLimits(-1,1);
            while (true) {
                getAngle();
                double error = controllerAngle.getOutput(getAngle(), goalAngle);


                telemetry.addData("Angle:", getAngle()); //Gives our current pos
                telemetry.addData("Error:", error);
                telemetry.addData("Global Subtract", globalAngle);
                telemetry.addData("Goal", goalAngle);
                telemetry.update();


                error = error*robot.turnFactorPID ;

                robot.frontrightDrive.setPower(-error);
                robot.backrightDrive.setPower(error);
                robot.frontleftDrive.setPower(-error);
                robot.backleftDrive.setPower(error);


                double abserr = Math.abs(getAngle() - goalAngle);

                if(abserr <= robot.tolerancePID){
                    robot.frontrightDrive.setPower(0);
                    robot.backrightDrive.setPower(0);
                    robot.frontleftDrive.setPower(0);
                    robot.backleftDrive.setPower(0);
                    break;
                }

            }


        }
        public void launch2shots() {
            robot.shooterDrive.setPower(.67);


            robot.triggerServo.setPosition(.55);
            sleep(140);
            robot.triggerServo.setPosition(.43);
            sleep(140);
            robot.triggerServo.setPosition(.55);
            sleep(140);
            robot.triggerServo.setPosition(.43);
            sleep(140);
            robot.shooterDrive.setPower(0);
        }





        @Override
        public void runOpMode(){
            robot.init(hardwareMap);
            telemetry.addData("Imu Status", robot.imu.getSystemStatus());
            telemetry.addData("Calibration Status", robot.imu.getCalibrationStatus());

            waitForStart();
            setAngle();
            //Code above here should never change
            while(!isStopRequested()) {

                drivePID(.5,0,1,40);
                turnToAnglePID(90);
                drivePID(.5,90,1, 40);
                turnToAnglePID(180);
                drivePID(.5,180,1, 40);
                turnToAnglePID(-90);
                drivePID(.5,-90,1, 40);
                turnToAnglePID(0);
                drivePID(.5,0,1, 40);
                turnToAnglePID(180);
                drivePID(.5,180,1, 40);
                turnToAnglePID(-90);
                drivePID(.5,-90,1, 40);
                stopDrive();







                // Dont put code below here
                stopDrive();
                break;
            }
            stopDrive();
            stop();
        }
    }

