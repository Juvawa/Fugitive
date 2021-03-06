#include "mbed.h"
#include "AndroidAccessory.h"
#include "DriveControl.h"

//device setup

//PwmOut red_top(p26);
PwmOut wheel1(p23);
PwmOut wheel2(p24);
PwmOut wheel3(p25);
DriveControl myDrive(wheel1,wheel2,wheel3);

DigitalOut led1(LED1);
DigitalOut led2(LED2);
DigitalOut led3(LED3);
DigitalOut led4(LED4);
AnalogIn Right(p19);
AnalogIn Left(p20);
    
DigitalOut ind(p21);
DigitalOut blinkL(p18);
DigitalOut blinkR(p15);

#define OUTL 100
#define INBL 100

class AdkTerm :public AndroidAccessory
{
public:
    AdkTerm():AndroidAccessory(INBL,OUTL,
                                   "ARM",
                                   "mbed",
                                   "mbed Terminal",
                                   "0.1",
                                   "http://www.mbed.org",
                                   "0000000012345678"),pc(USBTX,USBRX) {};
    virtual int callbackRead(u8 *buff, int len);
    virtual void setupDevice();
    virtual void resetDevice();
    virtual int callbackWrite();

private:
    void serialIRQ();
    void onTick();
    void AttachTick();
    char buffer[OUTL];
    int bcount;
    Serial pc;
    //AnalogIn Right;
    //AnalogIn Left;
    Ticker tick;
    float right,left,rl,ll;
    int tl,tr;
    Timeout n;
    bool settick;
};



void AdkTerm::setupDevice()
{
    pc.baud(115200);
    pc.printf("Welcome to adkTerm (MbedSketch)\n\r");
    settick = false;
    pc.attach(this, &AdkTerm::serialIRQ, Serial::RxIrq);
    for (int i = 0; i<OUTL; i++) {
        buffer[i] = 0;
    }
    bcount = 0;
    //n.attach(this,&AdkTerm::AttachTick,5);
    //tick.attach(this,&AdkTerm::onTick,0.1);
}

void AdkTerm::AttachTick()
{
    if(!settick)tick.attach(this,&AdkTerm::onTick,0.04);
    settick = true;
}

void AdkTerm::onTick()
{

}

void AdkTerm::resetDevice()
{
    pc.printf("adkTerm reset\n\r");
    for (int i = 0; i<OUTL; i++) {
        buffer[i] = 0;
    }
    bcount = 0;
}

int AdkTerm::callbackRead(u8 *buf, int len)
{
    //pc.printf("%i  %s\n\r\n\n\n",len,buf);
    //led1 = 1;
    if(len==32) {
         //led2 = 1;
         myDrive.order(buf);
    }
    for (int i = 0; i<INBL; i++) {
        buf[i] = 0;
    }

    //AttachTick();

    return 0;
}

int AdkTerm::callbackWrite()
{

    //ind = false;
    return 0;
}


void AdkTerm::serialIRQ()
{
    buffer[bcount] = pc.getc();

    pc.putc(buffer[bcount]);

    if (buffer[bcount] == '\n' || buffer[bcount] == '\r') {
        u8* wbuf = _writebuff;
        for (int i = 0; i<OUTL; i++) {
            wbuf[i] = buffer[i];
            buffer[i] = 0;
        }
        pc.printf("Sending: %s\n\r",wbuf);
        //ind = true;
        this->write(wbuf,bcount);
        bcount = 0;
    } else {
        if (buffer[bcount] != 0x08 && buffer[bcount] != 0x7F ) {
            bcount++;
            if (bcount == OUTL) {
                bcount = 0;
            }
        } else {
            bcount--;
        }
    }

}

AdkTerm AdkTerm;

int main()
{

    AdkTerm.setupDevice();
    printf("Android Development Kit: start\r\n");
    USBInit();
   
    while (1) {
        USBLoop();
    }
}
