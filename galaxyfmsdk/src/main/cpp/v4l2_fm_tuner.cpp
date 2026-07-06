#include "v4l2_fm_tuner.h"
#include <android/log.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/videodev2.h>
#include <cstring>
#include <cmath>

#define LOG_TAG "GalaxyFM-V4L2"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Qualcomm SM6375 / WCN3990 FM V4L2 Private Control IDs
#ifndef V4L2_CID_PRIVATE_BASE
#define V4L2_CID_PRIVATE_BASE 0x08000000
#endif

#ifndef V4L2_CTRL_CLASS_FM_RX
#define V4L2_CTRL_CLASS_FM_RX 0x009b0000
#endif

#ifndef V4L2_CID_FM_RX_CLASS_BASE
#define V4L2_CID_FM_RX_CLASS_BASE (V4L2_CTRL_CLASS_FM_RX | 0x900)
#endif

#ifndef V4L2_CID_TUNER_DEEMPHASIS
#define V4L2_CID_TUNER_DEEMPHASIS (V4L2_CID_FM_RX_CLASS_BASE + 1)
#endif

#ifndef V4L2_DEEMPHASIS_DISABLED
#define V4L2_DEEMPHASIS_DISABLED 0
#define V4L2_DEEMPHASIS_50_uS    1
#define V4L2_DEEMPHASIS_75_uS    2
#endif

#define V4L2_CID_PRV_STATE              (V4L2_CID_PRIVATE_BASE + 1)
#define V4L2_CID_PRV_AUDIO_PATH         (V4L2_CID_PRIVATE_BASE + 2)
#define V4L2_CID_PRV_SRCHMODE           (V4L2_CID_PRIVATE_BASE + 3)
#define V4L2_CID_PRV_SCANTIME           (V4L2_CID_PRIVATE_BASE + 4)
#define V4L2_CID_PRV_SIGNAL_TH          (V4L2_CID_PRIVATE_BASE + 5)
#define V4L2_CID_PRV_RDS_ON             (V4L2_CID_PRIVATE_BASE + 6)

V4l2FmTuner::V4l2FmTuner() : mFd(-1), mCurrentFreqMhz(87.5f), mPowerState(false) {
    mCachedRds = {"", "", 0, 0, false, false};
}

V4l2FmTuner::~V4l2FmTuner() {
    closeDevice();
}

bool V4l2FmTuner::openDevice(const std::string& devicePath) {
    if (mFd >= 0) {
        LOGI("Device already opened: %d", mFd);
        return true;
    }
    // Attempt requested device path, followed by common Qualcomm Snapdragon 695 V4L2 nodes
    const char* possibleNodes[] = {
        devicePath.c_str(),
        "/dev/radio0",
        "/dev/v4l2-radio0",
        "/dev/wcn3990_fm",
        "/dev/msm_fm"
    };
    for (const char* node : possibleNodes) {
        if (!node || node[0] == '\0') continue;
        mFd = open(node, O_RDWR | O_NONBLOCK);
        if (mFd >= 0) {
            LOGI("Successfully opened hardware V4L2 FM device node: %s (fd=%d)", node, mFd);
            return true;
        }
    }
    LOGE("Failed to open any physical V4L2 FM hardware nodes (last errno=%d). Offline FM radio hardware access required.", errno);
    return false;
}

void V4l2FmTuner::closeDevice() {
    if (mFd >= 0) {
        powerDown();
        close(mFd);
        mFd = -1;
        LOGI("V4L2 FM device closed.");
    }
}

bool V4l2FmTuner::isOpened() const {
    return mFd >= 0;
}

bool V4l2FmTuner::powerUp(float freqMhz) {
    if (mFd < 0) {
        LOGE("Cannot powerUp: Hardware device node is not opened");
        return false;
    }
    mPowerState = true;
    mCurrentFreqMhz = freqMhz;
    setControl(V4L2_CID_AUDIO_MUTE, 0);
    setControl(V4L2_CID_PRV_STATE, 1);
    return setFrequency(freqMhz);
}

bool V4l2FmTuner::powerDown() {
    if (mFd < 0) {
        return false;
    }
    mPowerState = false;
    setControl(V4L2_CID_AUDIO_MUTE, 1);
    setControl(V4L2_CID_PRV_STATE, 0);
    return true;
}

bool V4l2FmTuner::setFrequency(float freqMhz) {
    if (mFd < 0) {
        LOGE("Cannot setFrequency: Hardware device node is not opened");
        return false;
    }
    mCurrentFreqMhz = freqMhz;
    struct v4l2_frequency vf;
    memset(&vf, 0, sizeof(vf));
    vf.tuner = 0;
    vf.type = V4L2_TUNER_RADIO;
    // V4L2 radio unit is 62.5 Hz -> freqMhz * 1e6 / 62.5 = freqMhz * 16000
    vf.frequency = static_cast<uint32_t>(std::round(freqMhz * 16000.0f));

    if (ioctl(mFd, VIDIOC_S_FREQUENCY, &vf) < 0) {
        LOGE("VIDIOC_S_FREQUENCY failed for %.2f MHz (errno=%d)", freqMhz, errno);
        return false;
    }
    LOGI("Successfully tuned V4L2 physical hardware to %.2f MHz", freqMhz);
    return true;
}

float V4l2FmTuner::getFrequency() {
    if (mFd < 0) {
        return mCurrentFreqMhz;
    }
    struct v4l2_frequency vf;
    memset(&vf, 0, sizeof(vf));
    vf.tuner = 0;
    if (ioctl(mFd, VIDIOC_G_FREQUENCY, &vf) == 0) {
        mCurrentFreqMhz = static_cast<float>(vf.frequency) / 16000.0f;
    }
    return mCurrentFreqMhz;
}

bool V4l2FmTuner::seek(bool seekUp, bool wrapAround) {
    if (mFd < 0) {
        LOGE("Cannot seek: Hardware device node is not opened");
        return false;
    }
    struct v4l2_hw_freq_seek seekReq;
    memset(&seekReq, 0, sizeof(seekReq));
    seekReq.tuner = 0;
    seekReq.type = V4L2_TUNER_RADIO;
    seekReq.seek_upward = seekUp ? 1 : 0;
    seekReq.wrap_around = wrapAround ? 1 : 0;

    if (ioctl(mFd, VIDIOC_S_HW_FREQ_SEEK, &seekReq) < 0) {
        LOGW("VIDIOC_S_HW_FREQ_SEEK ioctl failed (errno=%d)", errno);
        return false;
    }
    return true;
}

int V4l2FmTuner::getRssi() {
    if (mFd < 0) {
        return -120; // Minimum noise floor
    }
    struct v4l2_tuner vt;
    memset(&vt, 0, sizeof(vt));
    vt.index = 0;
    if (ioctl(mFd, VIDIOC_G_TUNER, &vt) == 0) {
        // V4L2 signal value is 0..65535, map to approx dBm [-120 .. -30]
        int level = static_cast<int>(vt.signal);
        return -120 + (level * 90 / 65535);
    }
    return -120;
}

bool V4l2FmTuner::isStereo() {
    if (mFd < 0) {
        return false;
    }
    struct v4l2_tuner vt;
    memset(&vt, 0, sizeof(vt));
    vt.index = 0;
    if (ioctl(mFd, VIDIOC_G_TUNER, &vt) == 0) {
        return (vt.rxsubchans & V4L2_TUNER_SUB_STEREO) != 0;
    }
    return false;
}

FmRdsNativeData V4l2FmTuner::getRdsData() {
    if (mFd < 0) {
        return {"", "", 0, 0, false, false};
    }
    struct v4l2_rds_data {
        uint8_t lsb;
        uint8_t msb;
        uint8_t block;
    } rds_buf[16];

    ssize_t bytes_read = read(mFd, rds_buf, sizeof(rds_buf));
    if (bytes_read >= (ssize_t)sizeof(rds_buf[0])) {
        size_t blocks = bytes_read / sizeof(rds_buf[0]);
        for (size_t i = 0; i < blocks; i++) {
            uint16_t val = (static_cast<uint16_t>(rds_buf[i].msb) << 8) | rds_buf[i].lsb;
            uint8_t blk = rds_buf[i].block & 0x07;
            if (blk == 0) { // Block A = PI code
                mCachedRds.piCode = val;
            }
        }
    }
    return mCachedRds;
}

bool V4l2FmTuner::setMute(bool mute) {
    return setControl(V4L2_CID_AUDIO_MUTE, mute ? 1 : 0);
}

bool V4l2FmTuner::setBand(int band) {
    // 0 = US/EU 87.5-108 MHz
    return setControl(V4L2_CID_TUNER_DEEMPHASIS, band == 0 ? V4L2_DEEMPHASIS_75_uS : V4L2_DEEMPHASIS_50_uS);
}

bool V4l2FmTuner::setDeEmphasis(int emphasis) {
    return setControl(V4L2_CID_TUNER_DEEMPHASIS, emphasis == 0 ? V4L2_DEEMPHASIS_75_uS : V4L2_DEEMPHASIS_50_uS);
}

bool V4l2FmTuner::setVolume(int volume) {
    return setControl(V4L2_CID_AUDIO_VOLUME, volume);
}

bool V4l2FmTuner::setControl(uint32_t id, int32_t value) {
    if (mFd < 0) return false;
    struct v4l2_control ctrl;
    memset(&ctrl, 0, sizeof(ctrl));
    ctrl.id = id;
    ctrl.value = value;
    if (ioctl(mFd, VIDIOC_S_CTRL, &ctrl) < 0) {
        LOGW("VIDIOC_S_CTRL id=0x%X val=%d failed (errno=%d)", id, value, errno);
        return false;
    }
    return true;
}

int32_t V4l2FmTuner::getControl(uint32_t id) {
    if (mFd < 0) return 0;
    struct v4l2_control ctrl;
    memset(&ctrl, 0, sizeof(ctrl));
    ctrl.id = id;
    if (ioctl(mFd, VIDIOC_G_CTRL, &ctrl) == 0) {
        return ctrl.value;
    }
    return 0;
}
