struct BranchID {
  1:string name;
  2:string ip;
  3:i32 port;
}

struct TransferMessage {
  1:BranchID orig_branchId; # sender branch's ID
  2:i32 amount;
}

struct LocalSnapshot {
  1:i32 snapshot_num; 
  2:i32 balance;
  3:list<i32> messages;
}

exception SystemException {
  1: optional string message
}

service Branch {
  void initBranch(1:i32 balance, 2:list<BranchID> all_branches) 
      throws (1: SystemException systemException),

  void transferMoney(1:TransferMessage message) 
      throws (1: SystemException systemException),

  void initSnapshot(1:i32 snapshot_num)
      throws (1: SystemException systemException),
  
  void Marker(1:BranchID branchId, 2:i32 snapshot_num)
      throws (1: SystemException systemException),

  LocalSnapshot retrieveSnapshot(1:i32 snapshot_num)
      throws (1: SystemException systemException),
}
