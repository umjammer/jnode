/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.fs.jfat;

import java.io.IOException;

import org.jnode.driver.block.BlockDeviceAPI;


/**
 * BootSector.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/08 umjammer initial version <br>
 */
public interface BootSector {

    boolean isaValidBootSector();

    void read(BlockDeviceAPI device) throws IOException;

    void write(BlockDeviceAPI device, long offset) throws IOException;

    /**
     * @return "FAT" + #fatSize()
     */
    String fatType();

    boolean isDirty();

    boolean isFat12();

    boolean isFat16();

    boolean isFat32();

    /**
     * @return the FAT size: 12, 16, 32
     */
    int fatSize();

    /**
     *
     * @return BPB_Media
     */
    int getMediumDescriptor();

    long getSectorsPerFat();

    int getBytesPerSector();

    /**
     * bytes a cluster
     */
    int getClusterSize();

    int getSectorsPerCluster();

    int getNrReservedSectors();

    int getNrFats();

    long getRootDirectoryStartCluster();

    long getCountOfClusters();

    long getFirstDataSector();

    long getNrRootDirEntries();

    String getVolumeLabel();

    long getVolumeId();

    String getOEMName();

    /**
     * The Setting methods are writing here.
     *
     */
    void setBS_JmpBoot(byte[] BS_jmpBoot);

    void setBS_OemName(String BS_OEMName);

    void setBPB_BytesPerSector(int BPB_BytsPerSec);

    void setBPB_SecPerCluster(int BPB_SecPerClus);

    void setBPB_RsvSecCount(int BPB_RsvdSecCnt);

    void setBPB_NoFATs(int BPB_NumFATs);

    void setBPB_RootEntCnt(int BPB_RootEntCnt);

    void setBPB_TotSec16(int BPB_TotSec16);

    void setBPB_MediumDescriptor(int BPB_Media);

    void setBPB_FATSz16(int BPB_FATSz16);

    void setBPB_SecPerTrk(int BPB_SecPerTrk);

    void setBPB_NumHeads(int BPB_NumHeads);

    void setBPB_HiddSec(long BPB_HiddSec);

    void setBPB_TotSec32(long BPB_TotSec32);

    void setBPB_FATSz32(long BPB_FATSz32);

    void setBPB_ExtFlags(int BPB_ExtFlags);

    void setBPB_FSVer(int BPB_FSVer);

    void setBPB_RootClus(long BPB_RootClus);

    void setBPB_FSInfo(int BPB_FSInfo);

    void setBPB_BkBootSec(int BPB_BkBootSec);

    void setBPB_Reserved(byte[] BPB_Reserved);

    void setBS_DrvNum(int BS_DrvNum);

    void setBS_Reserved1(int BS_Reserved1);

    void setBS_BootSig(int BS_BootSig);

    void setBS_VolID(long BS_VolID);

    void setBS_VolLab(String BS_VolLab);

    void setBS_FilSysType(String BS_FilSysType);

    void setBS_Identifier(byte[] ident);
}
