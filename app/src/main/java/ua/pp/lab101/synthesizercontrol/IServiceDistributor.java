package ua.pp.lab101.synthesizercontrol;

import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;

/**
 * Created by ashram on 4/15/15.
 */
public interface IServiceDistributor {
    public BoardManagerService getService();
}
