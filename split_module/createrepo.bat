set SUBFOLDER=%1%
set GITLINK=%2%

f:
cd F:\Android\src_split\kds_premium
@echo off
echo remove remote information
@echo on
git remote rm origin
@echo off
echo convert to new %SUBFOLDER% repository
@echo on
git filter-branch --tag-name-filter cat --prune-empty --subdirectory-filter %SUBFOLDER% -- --all
@echo off
echo clear useless objects
@echo on

git reset --hard
rem git for-each-ref --format=¡®%(refname)¡¯ refs/original/ | xargs -n 1 git update-ref -d
git update-ref -d refs/original/refs/heads/KPP2-4
git update-ref -d refs/original/refs/heads/coke
git update-ref -d refs/original/refs/heads/master
git update-ref -d refs/original/refs/heads/stage
git reflog expire --expire=now --all
git gc --aggressive --prune=now

echo upload to %GITLINK%
git remote add origin %GITLINK%
git push -u origin --all
git push -u origin --tags

@echo off
echo ----- Done ---
pause