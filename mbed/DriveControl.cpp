#include "DriveControl.h"
#include "mbed.h"

/* DriveControl Constructor
 * 
 * Wheel1 and wheel2 are the rearside wheels, wheel3 is the front wheel
 * Code: Move + Direction (Forward, Backwards, Left, Right, Stop)
 * Send: MF/MB/ML/MR/MS
 */
 
DriveControl::DriveControl(PwmOut &wheel1,PwmOut &wheel2,PwmOut &wheel3) {
   this->wheel1 = &wheel1;
   this->wheel2 = &wheel2;
   this->wheel3 = &wheel3;
}

void DriveControl::forward() {
   wheel1->pulsewidth(0.01);
   wheel2->pulsewidth(0.01);
   wheel3->pulsewidth(0.0000);
}
void DriveControl::backward() {
   wheel1->pulsewidth(0.0005);
   wheel2->pulsewidth(0.0005);
   wheel3->pulsewidth(0.0000);
}
void DriveControl::turnleft() {
   wheel1->pulsewidth(0.01);
   wheel2->pulsewidth(0.01);
   wheel3->pulsewidth(0.01);
}
void DriveControl::turnright() {
   wheel1->pulsewidth(0.0005);
   wheel2->pulsewidth(0.0005);
   wheel3->pulsewidth(0.0005);
}

void DriveControl::stop() {
   wheel1->pulsewidth(0.0);
   wheel2->pulsewidth(0.0);
   wheel3->pulsewidth(0.0);
}

void DriveControl::order(u8 *code) {
      if(code == (u8*) "MF") {
         forward();
      }
      if(code == (u8*) "MB") {
         backward();
      }
      if(code == (u8*) "ML") {
         turnleft();
      }
      if(code == (u8*) "MR") {
         turnright();
      }
      if(code == (u8*) "MS") {
         stop();
      }
}
