#ifndef GALAXY_FM_V4L2_TUNER_H
#define GALAXY_FM_V4L2_TUNER_H

#include <string>
#include <vector>
#include <cstdint>

struct FmRdsNativeData {
    std::string psName;
    std::string radioText;
    int pty;
    int piCode;
    bool tp;
    bool ta;
};

class V4l2FmTuner {
public:
    V4l2FmTuner();
    ~V4l2FmTuner();

    bool openDevice(const std::string& devicePath = "/dev/radio0");
    void closeDevice();
    bool isOpened() const;

    bool powerUp(float freqMhz);
    bool powerDown();

    bool setFrequency(float freqMhz);
    float getFrequency();

    bool seek(bool seekUp, bool wrapAround);
    int getRssi();
    bool isStereo();
    FmRdsNativeData getRdsData();

    bool setMute(bool mute);
    bool setBand(int band); // 0: US/EU (87.5-108), 1: Japan (76-95)
    bool setDeEmphasis(int emphasis); // 0: 75us (US), 1: 50us (EU)
    bool setVolume(int volume); // 0-15

private:
    int mFd;
    float mCurrentFreqMhz;
    bool mPowerState;
    FmRdsNativeData mCachedRds;

    bool setControl(uint32_t id, int32_t value);
    int32_t getControl(uint32_t id);
};

#endif // GALAXY_FM_V4L2_TUNER_H
