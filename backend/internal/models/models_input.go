package models

type LLMResponseRequest struct {
	RequestID *string          `json:"request_id,omitempty"`
	Context   *ContextSnapshot `json:"context,omitempty"`
}

type ContextSnapshot struct {
	UUID     *string `json:"uuid,omitempty"`
	Key      *string `json:"key,omitempty"`
	HashCode *int64  `json:"hash_code,omitempty"`
	Version  *string `json:"version,omitempty"`

	TS    *string `json:"ts,omitempty"`
	TSRaw *int64  `json:"ts_raw,omitempty"`

	DayOfWeek  *int64 `json:"DayOfWeek,omitempty"`
	HourOfDay  *int64 `json:"HourOfDay,omitempty"`
	WorkingDay *int64 `json:"WorkingDay,omitempty"`

	AppValue        *string  `json:"App_Value,omitempty"`
	AudioMusic      *int64   `json:"Audio_Music,omitempty"`
	AudioHeadphones *int64   `json:"Audio_Headphones,omitempty"`
	BattValue       *int64   `json:"Batt_Value,omitempty"`
	BattDrainValue  *int64   `json:"BattDrain_Value,omitempty"`

	CellTowerData    *int64  `json:"CellTower_Data,omitempty"`
	CellTowerRoaming *int64  `json:"CellTower_Roaming,omitempty"`
	CellTowerType    *string `json:"CellTower_Type,omitempty"`
	CellTowerId      *int64  `json:"CellTower_Id,omitempty"`
	CellTowerLAC     *int64  `json:"CellTower_LAC,omitempty"`
	CellTowerGSMSig  *int64  `json:"CellTower_GSMSigStr,omitempty"`

	ChargingValue *int64   `json:"Charging_Value,omitempty"`
	LightValue    *int64   `json:"Light_Value,omitempty"`
	LocAcc        *float64 `json:"Loc_Acc,omitempty"`
	LocType       *string  `json:"Loc_Type,omitempty"`
	NoiseValue    *float64 `json:"Noise_Value,omitempty"`

	NotifApp         *string `json:"Notif_App,omitempty"`
	NotifCenterValue *int64  `json:"NotifCenter_Value,omitempty"`
	ProxValue        *int64  `json:"Prox_Value,omitempty"`
	ScrOrientValue   *string `json:"ScrOrient_Value,omitempty"`
	ScreenValue      *string `json:"Screen_Value,omitempty"`
	UserActType      *string `json:"UserAct_Type,omitempty"`
	UserActP         *int64  `json:"UserAct_p,omitempty"`

	WifiConn  *int64  `json:"Wifi_Conn,omitempty"`
	WifiNetID *int64  `json:"Wifi_NetID,omitempty"`
	WifiBSSID *string `json:"Wifi_BSSID,omitempty"`
	WifiSSID  *string `json:"Wifi_SSID,omitempty"`
	WifiIP    *string `json:"Wifi_IP,omitempty"`
	WifiRSSI  *int64  `json:"Wifi_RSSI,omitempty"`
	WifiSpeed *int64  `json:"Wifi_Speed,omitempty"`

	TimeSinceLastScreenUnlocked *int64  `json:"timeSinceLastScreenUnlocked,omitempty"`
	TimeSinceScreenChanged      *int64  `json:"timeSinceScreenChanged,omitempty"`
	TimeSinceLastMessageSession *int64  `json:"timeSinceLastMessageSession,omitempty"`
	TimeSinceLastCallPickup     *int64  `json:"timeSinceLastCallPickup,omitempty"`
	LastForegroundAppCategory   *string `json:"lastForegroundAppCategory,omitempty"`
	TimeSinceLastIncomingCall   *int64  `json:"timeSinceLastIncomingCall,omitempty"`
	TimeSinceLastOpenApp        *int64  `json:"timeSinceLastOpenApp,omitempty"`
	TimeSinceLastCallMade       *int64  `json:"timeSinceLastCallMade,omitempty"`
	TimeSinceLastCommApp        *int64  `json:"timeSinceLastCommApp,omitempty"`

	Temperature *float64 `json:"Temperature,omitempty"`
	Weather     *string  `json:"Weather,omitempty"`

	CalendarEvent *int64  `json:"Calendar_Event,omitempty"`
	EventName     *string `json:"Event_Name,omitempty"`
	EventStart    *int64  `json:"Event_Start,omitempty"`
	EventEnd      *int64  `json:"Event_End,omitempty"`
	EventDuration *int64  `json:"Event_Duration,omitempty"`
	EventTimeLeft *int64  `json:"Event_TimeLeft,omitempty"`
	EventLocation *string `json:"Event_Location,omitempty"`

	IsSilent        *int64  `json:"Is_Silent,omitempty"`
	BackgroundAmp   *int64  `json:"Background_Amp,omitempty"`
	BackgroundConvo *string `json:"Background_Convo,omitempty"`
	OnCall          *int64  `json:"On_Call,omitempty"`
	DoNotDisturb    *string `json:"DoNot_Disturb,omitempty"`
	RingerMode      *string `json:"Ringer_Mode,omitempty"`
	IsLauncher      *int64  `json:"Is_Launcher,omitempty"`

	PredictedAvailability *string `json:"Predicted_Availability,omitempty"`
}