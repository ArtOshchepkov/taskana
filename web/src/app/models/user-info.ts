
export class UserInfoModel {
  constructor(
    public userId: string = '',
    public groupIds: Array<string> = [],
    public roles: Array<string> = []
  ) { }
}
