#ifndef _ROBOTCONTROL_H_
#define _ROBOTCONTROL_H_

#include "mbed.h"


class DriveControl {
   PwmOut *wheel1,*wheel2,*wheel3;
   public:
      void forward();
      void backward();
      void turnleft();
      void turnright();
      void stop();
      void order(char code);
      
      DriveControl(PwmOut &wheel1,PwmOut &wheel2,PwmOut &wheel3);
};
#endif
