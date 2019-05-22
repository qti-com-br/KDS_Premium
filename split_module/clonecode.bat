
set FROMFOLDER=%1%
set TOFOLDER=%2%

cd %TOFOLDER%

echo clone from %FROMFOLDER% to %TOFOLDER%
mkdir kds_premium
git clone %FROMFOLDER%\kds_premium %TOFOLDER%\kds_premium
cd kds_premium
git.exe checkout -b KPP2-4 remotes/origin/KPP2-4 --
git.exe checkout -b coke remotes/origin/coke --
git.exe checkout -b master remotes/origin/master --
git.exe checkout -b stage remotes/origin/stage --

pause